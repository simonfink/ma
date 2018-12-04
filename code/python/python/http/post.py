from requests.auth import HTTPBasicAuth
import requests

user = "fsi"
password = "fsi"

url = "/api/exchanges/%2f/amq.topic/publish"
data = '{"properties":{},"routing_key":"test.smartvalve","payload":"python http post test","payload_encoding":"string"}'

s = requests.Session()

s.auth = ('fsi', 'fsi')

headers = {
    'Content-type':'application/x-www-form-urlencoded',
    'Accept':'application/json',
    'Accept-Encoding':'gzip, deflate'
}

r = s.post("http://172.18.252.25:15672/api/exchanges/%2f/amq.topic/publish", headers=headers, data=data)
#r = requests.post("http://172.18.252.25:15672/api/exchanges/%2f/amq.topic/publish", auth=(user,password), data=data)

print(r.text)
print(r.headers)
