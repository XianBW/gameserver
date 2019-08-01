import json

games = []
file = open("data_1.json",encoding='utf-8')
games = [json.loads(line.strip()) for line in file.readlines()]
file.close()
for game in games:
    
    game["price"] = game["price"][2:]
    print(game["price"])

