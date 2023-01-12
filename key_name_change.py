def returnNumber(string):
    for i in range(-1, -1000, -1):
        if string[i] == " ":
            return string[i + 1:-1] + string[-1]
        
def returnKey(string):
    for i in range(0, 1000):
        if string[i] == "[":
            start = i + 1
        elif string[i] == "]":
            return string[start:i]

if __name__ == "__main__":
    returnList = []
    for _ in range(int(input())):
        nameToChange = input()
        nameToChange.lstrip()
        if(nameToChange == ""): continue
        key = returnNumber(nameToChange)
        value = returnKey(nameToChange)
        returnList.append("cardNumberHashmap[" + str(key) + "] = " + value)
    for i in returnList:
        print(i)
        
        