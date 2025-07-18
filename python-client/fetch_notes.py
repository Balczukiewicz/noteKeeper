import requests
import json
import sys
from typing import List, Dict, Optional


class NoteKeeperClient:

    def __init__(self, base_url: str = "http://localhost:8080"):
        self.base_url = base_url
        self.token: Optional[str] = None
    
    def authenticate(self, username: str, password: str) -> bool:
        auth_url = f"{self.base_url}/api/v1/auth"
        
        payload = {
            "username": username,
            "password": password
        }
        
        headers = {
            "Content-Type": "application/json"
        }
        
        try:
            response = requests.post(auth_url, json=payload, headers=headers)
            response.raise_for_status()
            
            auth_data = response.json()
            self.token = auth_data.get("token")
            
            if self.token:
                print(f"Authentication successful for user: {username}")
                return True
            else:
                print("Authentication failed: No token received")
                return False
                
        except requests.exceptions.RequestException as e:
            print(f"✗ Authentication failed: {e}")
            if hasattr(e, 'response') and e.response is not None:
                try:
                    error_data = e.response.json()
                    print(f"  Error details: {error_data.get('message', 'Unknown error')}")
                except:
                    print(f"  HTTP {e.response.status_code}: {e.response.text}")
            return False
    
    def get_notes(self) -> Optional[List[Dict]]:
        if not self.token:
            print(" No authentication token available. Please authenticate first.")
            return None
        
        notes_url = f"{self.base_url}/api/v1/notes"
        
        headers = {
            "Authorization": f"Bearer {self.token}",
            "Content-Type": "application/json"
        }
        
        try:
            response = requests.get(notes_url, headers=headers)
            response.raise_for_status()
            
            notes = response.json()
            print(f"Successfully retrieved {len(notes)} notes")
            return notes
            
        except requests.exceptions.RequestException as e:
            print(f"Failed to retrieve notes: {e}")
            if hasattr(e, 'response') and e.response is not None:
                try:
                    error_data = e.response.json()
                    print(f"  Error details: {error_data.get('message', 'Unknown error')}")
                except:
                    print(f"  HTTP {e.response.status_code}: {e.response.text}")
            return None
    
    def create_sample_note(self, title: str, content: str) -> bool:
        if not self.token:
            print("No authentication token available. Please authenticate first.")
            return False
        
        notes_url = f"{self.base_url}/api/v1/notes"
        
        payload = {
            "title": title,
            "content": content
        }
        
        headers = {
            "Authorization": f"Bearer {self.token}",
            "Content-Type": "application/json"
        }
        
        try:
            response = requests.post(notes_url, json=payload, headers=headers)
            response.raise_for_status()
            
            note = response.json()
            print(f"Created note with ID: {note.get('id')}")
            return True
            
        except requests.exceptions.RequestException as e:
            print(f"Failed to create note: {e}")
            return False


def starts_with_vowel(text: str) -> bool:
    if not text:
        return False
    return text[0].lower() in 'aeiou'


def generate_report(notes: List[Dict]) -> None:
    print("\n" + "="*50)
    print(" NOTES REPORT")
    print("="*50)
    
    total_notes = len(notes)
    print(f"Total notes: {total_notes}")
    
    if total_notes == 0:
        print("No notes found.")
        return
    
    vowel_notes = [note for note in notes if starts_with_vowel(note.get('title', ''))]
    
    if vowel_notes:
        print("Titles starting with vowel:")
        for note in vowel_notes:
            print(f" - {note.get('title', 'Untitled')}")
    else:
        print("No titles starting with vowel found.")
    
    print("\n" + "="*50)
    print(" ALL NOTES DETAILS")
    print("="*50)
    
    for i, note in enumerate(notes, 1):
        print(f"{i}. ID: {note.get('id', 'N/A')}")
        print(f"   Title: {note.get('title', 'Untitled')}")
        print(f"   Content: {note.get('content', 'No content')[:100]}{'...' if len(note.get('content', '')) > 100 else ''}")
        print()


def main():
    print("Note Keeper API Client")
    print("="*30)
    
    client = NoteKeeperClient()
    
    username = "admin"
    password = "password"
    
    print(f"Attempting to authenticate with username: {username}")
    
    if not client.authenticate(username, password):
        print("Authentication failed. Please check your credentials and ensure the API is running.")
        sys.exit(1)
    
    print("\n Creating sample notes...")
    client.create_sample_note("An Important Note", "This is a very important note about something.")
    client.create_sample_note("Emergency Contact", "Call John at 555-1234 in case of emergency.")
    client.create_sample_note("Shopping List", "Milk, Bread, Eggs, Cheese, Apples")
    client.create_sample_note("Meeting Notes", "Discussed project timeline and deliverables.")
    client.create_sample_note("Ideas for Weekend", "Visit the park, read a book, call mom.")
    
    print("\n Retrieving notes...")
    notes = client.get_notes()
    
    if notes is None:
        print(" Failed to retrieve notes.")
        sys.exit(1)
    
    generate_report(notes)
    
    print("\n Client execution completed successfully!")


if __name__ == "__main__":
    main()
