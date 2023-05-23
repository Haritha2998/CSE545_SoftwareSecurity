import os
from xml.dom import minidom
import json
import heapq
from heapq import heappop
import matplotlib.pyplot as plt

# To run this file 
# source .venv/bin/activate
# python ss2.py

class Pair:
    def __init__(self, x, y):
        self.x = x
        self.y = y
 
    # Override the less-than operator __lt__ to make Pair class work with max heap
    def __lt__(self, other):
        return self.x > other.x
 
    def __repr__(self):
        return f'({self.x}, {self.y})'
    
def top10frequentPermissions():
    f = open('final_out.json')
    data = json.load(f)
    print("No of apk files: ",len(data.keys()))
    print("Top 10 Frequent Permissions are ")
    permissions = {}
    for i in data.keys():
        for each in data[i]:
            if each not in permissions:
                permissions[each] = 1
            else:
                permissions[each] +=1
    new_list = []
    for i in permissions.keys():
        new_list.append(Pair(permissions[i],i))
    k = 10
    heapq.heapify(new_list)
    while(k):
        print(heappop(new_list))
        k -= 1

def write_json():
    list_of_apks = os.listdir('selectedAPKs')
    size = len(list_of_apks)
    final_dict = {}
    for i in range(0,size):
        string = "apktool d selectedAPKs/" +list_of_apks[i] + " -o decode/"+list_of_apks[i]
        os.system(string)
        file = minidom.parse("decode/"+list_of_apks[i]+'/AndroidManifest.xml')
        models = file.getElementsByTagName('uses-permission')
        final_dict[list_of_apks[i]] = []
        for elem in models:
            final_dict[list_of_apks[i]].append(elem.attributes['android:name'].value)
    json_object = json.dumps(final_dict, indent=4)
    
    with open("final_out.json","w") as file:
        file.write(json_object)

def top10apps():
    f = open('final_out.json')
    print("Top 10 Apps with most permissions are ")
    data = json.load(f)
    new_list_apps = []
    for i in data.keys():
        new_list_apps.append(Pair(len(data[i]),i))
    heapq.heapify(new_list_apps)
    k = 10
    while(k):
        print(heappop(new_list_apps))
        k -= 1

def linechart():
    f = open("final_out.json")
    data = json.load(f)
    new_list_app = {}
    for i in data.keys():
        if len(data[i]) in new_list_app:
            new_list_app[len(data[i])] += 1
        else:
            new_list_app[len(data[i])] = 1
    x_axis = []
    y_axis = []
    #print(x_axis)
    for i in sorted(new_list_app.keys()):
        x_axis.append(i)
        y_axis.append(new_list_app[i])
    #print(x_axis)
    plt.plot(x_axis, y_axis)
    plt.title('Line Chart')
    plt.xlabel('No Of Permission Requested By The App')
    plt.ylabel('No Of Apps That Request a Specific Number Of Permissions')
    plt.show()

write_json()
top10frequentPermissions()
top10apps()
linechart()