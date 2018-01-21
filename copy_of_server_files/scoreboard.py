import pymongo
import pprint

uri = "mongodb://username:password@ds111138.mlab.com:11138/some-mlab-db"

client = pymongo.MongoClient(uri)
database = client.get_default_database()
scoreboard = database.scoreboard


"""
| Name | Score | _id |
"""


def start_player(_name):
    post = {
        "name": _name,
        "health": 5,
        "points": 0
    }
    post_id = scoreboard.insert_one(post).inserted_id
    return post_id


def register_hit(shooter, target):
    scoreboard.update_one(
        {'name': shooter},
        {'$inc': {'points': 100}}
    )
    scoreboard.update_one(
        {'name': target},
        {'$inc': {'health': -1}}
    )
    return None

def get_table():
    result = scoreboard.find()
    result = list(result)
    return result

def show_table():
    result = scoreboard.find()
    result = list(result)
    pprint.pprint(result)
    
def empty_table():
    scoreboard.drop()
    return None



###########
# TESTING #
###########
if __name__ == '__main__':
    empty_table()
    start_player("Alice")
    start_player("Bob")
    start_player("Charlie")
    show_table()
    register_hit("Alice", "Bob")
    register_hit("Alice", "Bob")
    register_hit("Alice", "Bob")
    register_hit("Alice", "Charlie")
    register_hit("Alice", "Charlie")
    show_table()
