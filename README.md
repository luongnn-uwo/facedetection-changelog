# facedetect
Face recognition for class attendance using OpenCV\
For better recognition, next updated version will add ML, implementing TensorFlowLite.\ 
"--------------------------------------------------------------------------------------------------------------------------------------"\
This is an Android app I built for my MEng project. \
The idea is to recognize students in class and update attendance.\
MainActivity has 5 methods and 1 list:\
    <p>1. ListView: View all input photos in Gallery. At first installation it will load Gallery with some Photo samples.\
    \
    2. InputFace: To manually take photo of student for input. Student name is input once only, then it will keep updating. \
                  Existing student photo will be saved in the existing folder. \
                  Face detection used Haarcascade\
    \
    3. Recognize: To recognize student face once training was done. After that, it will update Attendance list accordingly upon confirm. \
                     Face detection used Haarcascade\
                     Face recognition used LBPHFaceRecognizer\
    \
    4. Train : training classifier based on photo in Gallery. If there is no photo, need to go to InputFace.\
    \
    5. Email: send the Attendance list as CSV file via other Email app. \
    \
    6. Attendance list is based on existing sample input Classlist.csv at Root Folder.\
       For future use, Classlist should be replaced by a proper one which will be downloaded later.\
       Classlist has 2 fields: 'ID' and 'Name' while AttendanceList has 4 fields: 'ID', 'Name', 'Status' and 'Date'.\
       AttedanceList will be updated by date and kept within a day, allowing multiple Recognitions within one day.</p>
              
--------------------------------------------------------------------------------------------------------------------------------------    
