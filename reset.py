import requests

url = 'https://blooming-hollows-76968.herokuapp.com/start_newgame'
data = {"data" : "Alice,Bob,Charlie"}
print(requests.post(url, data=data))