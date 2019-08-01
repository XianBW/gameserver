import json

fin = open("test.json",encoding="utf-8")
results = [json.loads(line.strip()) for line in fin.readlines()]


print(results[0]["username"])
fin.close()