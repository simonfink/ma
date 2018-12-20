import http.client, urllib
from base64 import b64encode

userAndPass = b64encode(b"fsi:fsi").decode("ascii")

params = urllib.parse.urlencode({"properties":{}, "routing_key":"test.smartvalve", "payload":"testpost with python","payload_encoding":"string"})
headers = {"Authorization":"Basic %s" % userAndPass,"Content-type":"application/x-www-form-urlencoded", "Accept":"text/plain"}

conn = http.client.HTTPConnection("172.18.252.25:15672")
conn.request("POST", "/api/exchanges/%2f/amq.topic/publish", params, headers)

response = conn.getresponse()

print(response.status, response.reason)
