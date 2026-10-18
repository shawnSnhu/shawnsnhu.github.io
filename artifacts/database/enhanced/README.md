# Animal Shelter Dashboard

This is the enhanced version of my Animal Shelter project. It keeps the original Python, MongoDB, and Dash setup while moving filtering, sorting, pagination, and breed counts into MongoDB.

## Run the project

First, from a terminal in the `enhanced` directory, create a local data folder and start MongoDB:

```
mkdir mongodb-data
mongod --dbpath ./mongodb-data
```

Leave MongoDB running, open a second terminal in the `enhanced` directory, and run:

```
python3 -m venv .venv
source .venv/bin/activate
python3 -m pip install -r requirements.txt

export MONGODB_URI="mongodb://localhost:27017"
export MONGODB_DATABASE="aac"
export MONGODB_COLLECTION="animals"

python3 load_sample_data.py
jupyter lab ProjectTwoDashboard.ipynb
```

The sample loader adds 12 sample animal records. It can be run again without creating duplicates.

If the database requires a username and password, include them in `MONGODB_URI` in the terminal. Do not put the actual value in the notebook.

## Run the tests

```
python3 -m unittest discover -s tests -v
```

The tests use a temporary in-memory MongoDB replacement.
