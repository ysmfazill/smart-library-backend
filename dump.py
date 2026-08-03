import sys
with open('src/main/resources/application.properties', 'rb') as f:
    content = f.read()
    print(repr(content))
