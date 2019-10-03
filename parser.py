import json
with open('input.json') as f:
	data = json.load(f)
	n = data['Towers']
	simulationTime = data['Simulation Time']
	dblocation = data['DBlocation']

with open('new_setting12.txt', 'r') as file:
    data = file.readlines()
data[0]='DBSelect=False\n'
data[133]='Group3.activeTimes = 0,43200\n'
data[145]='Group4.speed = 3.7,5.1\n'
with open('new_setting12.txt', 'w') as file:
   file.writelines( data )
