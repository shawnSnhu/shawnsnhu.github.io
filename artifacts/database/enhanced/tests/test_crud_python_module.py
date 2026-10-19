import os
import unittest
from unittest.mock import MagicMock, patch

import mongomock

from CRUD_Python_Module import AnimalShelter

ANIMALS = [
    {
        "animal_id": "A001",
        "name": "Bay",
        "animal_type": "Dog",
        "breed": "Labrador Retriever Mix",
        "sex_upon_outcome": "Intact Female",
        "age_upon_outcome_in_weeks": 52,
        "location_lat": 30.70,
        "location_long": -97.40,
    },
    {
        "animal_id": "A002",
        "name": "River",
        "animal_type": "Dog",
        "breed": "Newfoundland",
        "sex_upon_outcome": "Intact Female",
        "age_upon_outcome_in_weeks": 100,
        "location_lat": 30.71,
        "location_long": -97.41,
    },
    {
        "animal_id": "A003",
        "name": "Scout",
        "animal_type": "Dog",
        "breed": "German Shepherd",
        "sex_upon_outcome": "Intact Male",
        "age_upon_outcome_in_weeks": 100,
        "location_lat": 30.72,
        "location_long": -97.42,
    },
    {
        "animal_id": "A004",
        "name": "Alpine",
        "animal_type": "Dog",
        "breed": "Siberian Husky",
        "sex_upon_outcome": "Intact Male",
        "age_upon_outcome_in_weeks": 52,
        "location_lat": 30.73,
        "location_long": -97.43,
    },
    {
        "animal_id": "A005",
        "name": "Tracker",
        "animal_type": "Dog",
        "breed": "Bloodhound",
        "sex_upon_outcome": "Intact Male",
        "age_upon_outcome_in_weeks": 100,
        "location_lat": 30.74,
        "location_long": -97.44,
    },
    {
        "animal_id": "A006",
        "name": "Rocky",
        "animal_type": "Dog",
        "breed": "Rottweiler",
        "sex_upon_outcome": "Intact Male",
        "age_upon_outcome_in_weeks": 400,
        "location_lat": 30.75,
        "location_long": -97.45,
    },
    {
        "animal_id": "A007",
        "name": "Pixel",
        "animal_type": "Cat",
        "breed": "Domestic Shorthair",
        "sex_upon_outcome": "Spayed Female",
        "age_upon_outcome_in_weeks": 80,
        "location_lat": 30.76,
        "location_long": -97.46,
    },
    {
        "animal_id": "A008",
        "name": "Creek",
        "animal_type": "Dog",
        "breed": "Labrador Retriever Mix",
        "sex_upon_outcome": "Intact Female",
        "age_upon_outcome_in_weeks": 10,
        "location_lat": 30.77,
        "location_long": -97.47,
    },
    {
        "animal_id": "A009",
        "name": "Ranger",
        "animal_type": "Dog",
        "breed": "Rottweiler",
        "sex_upon_outcome": "Intact Male",
        "age_upon_outcome_in_weeks": 200,
        "location_lat": 30.78,
        "location_long": -97.48,
    },
]


class AnimalShelterTest(unittest.TestCase):
    def setUp(self):
        self.client = mongomock.MongoClient()
        self.shelter = AnimalShelter(client=self.client)
        self.shelter.collection.insert_many(ANIMALS)

    def ids_for_filter(self, filter_type):
        result = self.shelter.search_animals(
            filter_type=filter_type,
            page=0,
            page_size=100,
            sort_field="animal_id",
        )
        return [record["animal_id"] for record in result["records"]]

    # Rescue-filter tests verify that the predefined database queries select correctly.
    def test_water_filter(self):
        self.assertEqual(["A001", "A002"], self.ids_for_filter("water"))

    def test_mountain_filter(self):
        self.assertEqual(["A003", "A004"], self.ids_for_filter("mountain"))

    def test_disaster_filter(self):
        self.assertEqual(
            ["A003", "A005", "A009"], self.ids_for_filter("disaster")
        )

    def test_reset_filter_returns_all_records(self):
        result = self.shelter.search_animals(page_size=20)
        self.assertEqual(9, result["total_count"])
        self.assertEqual(9, len(result["records"]))

    def test_unknown_filter_is_rejected(self):
        with self.assertRaises(ValueError):
            self.shelter.search_animals(filter_type="$where")

    # Paging and sorting tests check the database-side operations used by the table.
    def test_pagination_returns_the_requested_page(self):
        first = self.shelter.search_animals(page=0, page_size=3)
        second = self.shelter.search_animals(page=1, page_size=3)

        self.assertEqual(["A001", "A002", "A003"], [a["animal_id"] for a in first["records"]])
        self.assertEqual(["A004", "A005", "A006"], [a["animal_id"] for a in second["records"]])
        self.assertEqual(3, first["total_pages"])

    def test_sorting_ascending(self):
        result = self.shelter.search_animals(page_size=20, sort_field="name")
        names = [animal["name"] for animal in result["records"]]
        self.assertEqual(sorted(names), names)

    def test_sorting_descending(self):
        result = self.shelter.search_animals(
            page_size=20, sort_field="animal_id", sort_order="desc"
        )
        self.assertEqual("A009", result["records"][0]["animal_id"])

    def test_invalid_page_values_are_rejected(self):
        invalid_values = [(-1, 10), (0, 0), (0, 101), (True, 10), (0, False)]
        for page, page_size in invalid_values:
            with self.subTest(page=page, page_size=page_size):
                with self.assertRaises(ValueError):
                    self.shelter.search_animals(page=page, page_size=page_size)

    def test_invalid_sort_options_are_rejected(self):
        with self.assertRaises(ValueError):
            self.shelter.search_animals(sort_field="$natural")
        with self.assertRaises(ValueError):
            self.shelter.search_animals(sort_order="sideways")

    def test_breed_counts_use_aggregation(self):
        counts = self.shelter.breed_counts()
        self.assertEqual(
            [
                {"breed": "Labrador Retriever Mix", "count": 2},
                {"breed": "Rottweiler", "count": 2},
            ],
            counts[:2],
        )

    # CRUD security tests confirm ordinary changes work while mass changes are blocked.
    def test_basic_crud_operations(self):
        new_animal = {
            "animal_id": "A010",
            "name": "Nova",
            "animal_type": "Dog",
        }
        self.assertTrue(self.shelter.create(new_animal))
        self.assertEqual(1, len(self.shelter.read({"animal_id": "A010"})))
        self.assertEqual(
            1, self.shelter.update({"animal_id": "A010"}, {"name": "Comet"})
        )
        self.assertEqual("Comet", self.shelter.read({"animal_id": "A010"})[0]["name"])
        self.assertEqual(1, self.shelter.delete({"animal_id": "A010"}))

    def test_empty_mass_update_and_delete_are_blocked(self):
        with self.assertRaises(ValueError):
            self.shelter.update({}, {"animal_type": "Unknown"})
        with self.assertRaises(ValueError):
            self.shelter.delete({})

    # Configuration tests keep real credentials and live MongoDB connections out of the suite.
    @patch.dict(os.environ, {}, clear=True)
    def test_connection_requires_environment_uri(self):
        with self.assertRaises(ValueError):
            AnimalShelter()

    @patch.dict(
        os.environ,
        {
            "MONGODB_URI": "mongodb://localhost:27017",
            "MONGODB_DATABASE": "test_database",
            "MONGODB_COLLECTION": "test_collection",
        },
        clear=True,
    )
    @patch("CRUD_Python_Module.MongoClient")
    def test_connection_uses_environment_configuration(self, mongo_client):
        fake_client = MagicMock()
        mongo_client.return_value = fake_client

        shelter = AnimalShelter()

        mongo_client.assert_called_once_with("mongodb://localhost:27017")
        self.assertIs(shelter.database, fake_client["test_database"])
        self.assertIs(
            shelter.collection,
            fake_client["test_database"]["test_collection"],
        )


if __name__ == "__main__":
    unittest.main()
