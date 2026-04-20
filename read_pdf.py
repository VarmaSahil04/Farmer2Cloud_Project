import PyPDF2

try:
    with open('bmd_report.pdf', 'rb') as file:
        reader = PyPDF2.PdfReader(file)
        print(f"Number of pages: {len(reader.pages)}")
        text = ""
        for i in range(len(reader.pages)):
            page_text = reader.pages[i].extract_text()
            if page_text:
                text += f"--- Page {i+1} ---\n{page_text}\n"
        
        with open('bmd_report_extracted.txt', 'w', encoding='utf-8') as out:
            out.write(text)
        print("Text extracted successfully to bmd_report_extracted.txt")
        # Print first 2000 chars to terminal
        print(text[:2000])
except Exception as e:
    print(f"Error: {e}")
