import PyPDF2
import sys

try:
    with open('SCAN DINE RESEARCH PAPER.pdf', 'rb') as file:
        reader = PyPDF2.PdfReader(file)
        print(f"Number of pages: {len(reader.pages)}")
        text = ""
        for i in range(len(reader.pages)):
            page_text = reader.pages[i].extract_text()
            if page_text:
                text += f"--- Page {i+1} ---\n{page_text}\n\n"
        
        with open('scan_dine_extracted.txt', 'w', encoding='utf-8') as out:
            out.write(text)
        print("Text extracted successfully!")
        print(text)
except Exception as e:
    print(f"Error: {e}")
