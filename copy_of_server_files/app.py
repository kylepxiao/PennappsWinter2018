from flask import Flask, request, jsonify
import scoreboard

app = Flask(__name__)


@app.route('/')
def home():
    return "hi there, this is the homepage"

#input:
#data="Alice,Bob,Charlie"
@app.route('/start_newgame', methods=['POST'])
def start_newgame():
    data = request.form["data"]
    names = data.split(",")
    scoreboard.empty_table()
    for n in names:
        scoreboard.start_player(n)
    return jsonify({
        'status': 'Success',
        'message': '/start_newgame finished!',
        'players': str(names)
        
    })

#input:
#data="Alice-hits-Bob"
@app.route('/register_hit', methods=["POST"])
def register_hit():
    data = request.form["data"]
    shooter, _, target = data.split("-")
    scoreboard.register_hit(shooter, target)
    query = scoreboard.get_table()
    score = -42
    for q in query:
        if q["name"] == shooter:
            score = q["points"]
    return jsonify({
        'status': 'Success',
        'message': '/register_hit finished!',
        'message2': shooter + " hit " + target,
        'score': score
    })
    

@app.route('/get_table')
def get_table():
    query = scoreboard.get_table()
    for q in query:
        del q["_id"]
    return jsonify(query)

@app.route('/empty_table')
def empty_table():
    scoreboard.empty_table()
    return jsonify({
        'status': 'Success',
        'message': 'scoreboard table has been emptied'
    })

#input:
#data="Alice"
@app.route('/get_player_info', methods=["POST"])
def get_player_info():
    name = request.form["data"]
    query = scoreboard.get_table()
    for q in query:
        del q["_id"]
    ret = "the player of name " + name + " was not found"
    for q in query:
        if q["name"] == name:
            ret = q
    return jsonify({
        "result": ret
    })

if __name__ == '__main__':
    app.debug = True
    app.run('0.0.0.0', 8000)
