import requests
import re

def scrape():
    url = "https://www.espn.com.ar/futbol/mundial/nota/_/id/13191646/mundial-2026-fixture-calendario-fecha-horarios-y-sedes"
    headers = {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36",
    }
    response = requests.get(url, headers=headers)
    
    # Just find all occurrences of Partido NN in the HTML
    matches = re.findall(r"(Partido 7[3-9].*?)(?:<br>|<\/p>|<)", response.text)
    for m in matches:
        print(m)
        
    matches2 = re.findall(r"(Partido 8[0-8].*?)(?:<br>|<\/p>|<)", response.text)
    for m in matches2:
        print(m)

if __name__ == "__main__":
    scrape()
