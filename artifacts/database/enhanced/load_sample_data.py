"""Load synthetic animal records for trying the dashboard locally."""

import json
import os
from pathlib import Path

from pymongo import MongoClient


def main():
    uri = os.getenv("MONGODB_URI")
    if not uri:
        raise ValueError("MONGODB_URI must be set before loading sample data")

    database_name = os.getenv("MONGODB_DATABASE", "aac")
    collection_name = os.getenv("MONGODB_COLLECTION", "animals")
    sample_path = Path(__file__).with_name("sample_animals.json")

    with sample_path.open(encoding="utf-8") as sample_file:
        animals = json.load(sample_file)

    client = MongoClient(uri)
    collection = client[database_name][collection_name]

    inserted = 0
    updated = 0
    for animal in animals:
        animal_id = animal.get("animal_id")
        if not animal_id:
            raise ValueError("Every sample record must contain an animal_id")

        result = collection.replace_one(
            {"animal_id": animal_id},
            animal,
            upsert=True,
        )
        if result.upserted_id is not None:
            inserted += 1
        else:
            updated += 1

    client.close()
    print(
        f"Loaded {len(animals)} sample animals "
        f"({inserted} inserted, {updated} updated)."
    )


if __name__ == "__main__":
    main()
