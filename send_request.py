import requests
url = 'https://eastus.api.cognitive.microsoft.com/face/v1.0/persongroups/lasertag/persons/f48e5c67-233a-43d2-89a1-b04d9b7c7f3c/persistedFaces'
for i in range(2, 12):
    data = open('./' + str(i) + '.jpg', 'rb').read()
    headers = {'Content-Type': 'application/octet-stream', 'Ocp-Apim-Subscription-Key': 'ddc8bd86b06b4a9ea2f12037e6d8a903'}
    print(requests.post(url, data=data, headers=headers).json())