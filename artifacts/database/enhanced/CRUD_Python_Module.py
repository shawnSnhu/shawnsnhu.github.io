"""MongoDB access for the animal shelter dashboard."""

import os
from copy import deepcopy

from pymongo import ASCENDING, DESCENDING, MongoClient


class AnimalShelter:
    """Provide CRUD and dashboard queries for the animal collection."""

    MAX_PAGE_SIZE = 100

    # Named filters prevent dashboard input from becoming arbitrary MongoDB queries.
    FILTER_QUERIES = {
        "reset": {},
        "water": {
            "animal_type": "Dog",
            "breed": {
                "$in": [
                    "Labrador Retriever Mix",
                    "Chesapeake Bay Retriever",
                    "Newfoundland",
                ]
            },
            "sex_upon_outcome": "Intact Female",
            "age_upon_outcome_in_weeks": {"$gte": 26, "$lte": 156},
        },
        "mountain": {
            "animal_type": "Dog",
            "breed": {
                "$in": [
                    "German Shepherd",
                    "Alaskan Malamute",
                    "Old English Sheepdog",
                    "Siberian Husky",
                    "Rottweiler",
                ]
            },
            "sex_upon_outcome": "Intact Male",
            "age_upon_outcome_in_weeks": {"$gte": 26, "$lte": 156},
        },
        "disaster": {
            "animal_type": "Dog",
            "breed": {
                "$in": [
                    "Doberman Pinscher",
                    "German Shepherd",
                    "Golden Retriever",
                    "Bloodhound",
                    "Rottweiler",
                ]
            },
            "sex_upon_outcome": "Intact Male",
            "age_upon_outcome_in_weeks": {"$gte": 20, "$lte": 300},
        },
    }

    # Only fields displayed by the dashboard may be used for server-side sorting.
    SORT_FIELDS = {
        "animal_id",
        "animal_type",
        "name",
        "breed",
        "sex_upon_outcome",
        "age_upon_outcome_in_weeks",
        "location_lat",
        "location_long",
    }

    def __init__(self, client=None):
        """Connect with environment configuration or use an injected test client."""
        if client is None:
            uri = os.getenv("MONGODB_URI")
            if not uri:
                raise ValueError("MONGODB_URI must be set before connecting")
            client = MongoClient(uri)

        database_name = os.getenv("MONGODB_DATABASE", "aac").strip()
        collection_name = os.getenv("MONGODB_COLLECTION", "animals").strip()
        if not database_name or not collection_name:
            raise ValueError("Database and collection names cannot be empty")

        self.client = client
        self.database = client[database_name]
        self.collection = self.database[collection_name]

    # basic CRUD methods are retained for compatibility with the original project
    def create(self, data):
        if not isinstance(data, dict) or not data:
            return False
        self.collection.insert_one(dict(data))
        return True

    def read(self, query, projection=None):
        if not isinstance(query, dict):
            return []
        return list(self.collection.find(query, projection))

    def update(self, query, new_values):
        # Empty filters are blocked so an accidental request cannot update every record.
        if not isinstance(query, dict) or not query:
            raise ValueError("An update query cannot be empty")
        if not isinstance(new_values, dict) or not new_values:
            raise ValueError("Update values cannot be empty")
        result = self.collection.update_many(query, {"$set": new_values})
        return result.modified_count

    def delete(self, query):
        # Deleting the entire collection requires an explicit filter, not an empty query.
        if not isinstance(query, dict) or not query:
            raise ValueError("A delete query cannot be empty")
        result = self.collection.delete_many(query)
        return result.deleted_count

    def search_animals(
        self,
        filter_type="reset",
        page=0,
        page_size=10,
        sort_field="animal_id",
        sort_order="asc",
    ):
        """Return one validated, sorted page of matching animal records."""
        query = self._query_for_filter(filter_type)
        self._validate_page(page, page_size)

        if sort_field not in self.SORT_FIELDS:
            raise ValueError(f"Unsupported sort field: {sort_field}")
        if sort_order not in {"asc", "desc"}:
            raise ValueError("Sort order must be 'asc' or 'desc'")

        sort_direction = ASCENDING if sort_order == "asc" else DESCENDING
        total_count = self.collection.count_documents(query)
        records_to_skip = page * page_size

        # MongoDB sorts and limits the result before records reach the dashboard.
        cursor = (
            self.collection.find(query, {"_id": 0})
            .sort(sort_field, sort_direction)
            .skip(records_to_skip)
            .limit(page_size)
        )
        records = list(cursor)
        total_pages = (total_count + page_size - 1) // page_size

        return {
            "records": records,
            "total_count": total_count,
            "page": page,
            "page_size": page_size,
            "total_pages": total_pages,
        }

    def breed_counts(self, filter_type="reset"):
        """Aggregate matching animals by breed in MongoDB."""
        query = self._query_for_filter(filter_type)
        pipeline = [
            {"$match": query},
            {"$group": {"_id": "$breed", "count": {"$sum": 1}}},
            {"$sort": {"count": -1, "_id": 1}},
        ]

        return [
            {"breed": item["_id"] or "Unknown", "count": item["count"]}
            for item in self.collection.aggregate(pipeline)
        ]

    @classmethod
    def _query_for_filter(cls, filter_type):
        if filter_type not in cls.FILTER_QUERIES:
            raise ValueError(f"Unsupported rescue filter: {filter_type}")
        return deepcopy(cls.FILTER_QUERIES[filter_type])

    @classmethod
    def _validate_page(cls, page, page_size):
        if isinstance(page, bool) or not isinstance(page, int) or page < 0:
            raise ValueError("Page must be a nonnegative integer")
        # The upper bound prevents a single dashboard request from returning too much data.
        if (
            isinstance(page_size, bool)
            or not isinstance(page_size, int)
            or page_size < 1
            or page_size > cls.MAX_PAGE_SIZE
        ):
            raise ValueError(
                f"Page size must be between 1 and {cls.MAX_PAGE_SIZE}"
            )
