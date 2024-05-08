
This README contains detailed feature descriptions for the three data sets provided with this assignment.


************************************************************************************
1.) The Student data set

Instances:
- Each instance (line in the file) represents a student.

Label: 
- The final grade (A+, A, B, C, D, F) indicating what score the student achieved.

Features: 
1 school - students school (binary: "GP" - Gabriel Pereira or "MS" - Mousinho da Silveira)
2 sex - students sex (binary: "F" - female or "M" - male)
3 address - students home address type (binary: "U" - urban or "R" - rural)
4 famsize - family size (binary: "LE3" - less or equal to 3 or "GT3" - greater than 3)
5 Pstatus - parents cohabitation status (binary: "T" - living together or "A" - apart)
6 Medu - mothers education (nominal: low, none, mid, mid, high)
7 Fedu - fathers education (nominal: low, none, mid, mid, high)
8 Mjob - mothers job (nominal: "teacher", "health" care related, civil "services" (e.g. administrative or police), "at_home" or "other")
9 Fjob - fathers job (nominal: "teacher", "health" care related, civil "services" (e.g. administrative or police), "at_home" or "other")
10 reason - reason to choose this school (nominal: close to "home", school "reputation", "course" preference or "other")
11 guardian - students guardian (nominal: "mother", "father" or "other")
12 traveltime - home to school travel time (nominal: none,  low, medium, high, very_high)
13 studytime - weekly study time (nominal: none,  low, medium, high, very_high)
14 failures - number of past class failures (nominal: none,  low, medium, high, very_high)
15 schoolsup - extra educational support (binary: yes or no)
16 famsup - family educational support (binary: yes or no)
17 paid - extra paid classes within the course subject (binary: yes or no)
18 activities - extra-curricular activities (binary: yes or no)
19 nursery - attended nursery school (binary: yes or no)
20 higher - wants to take higher education (binary: yes or no)
21 internet - Internet access at home (binary: yes or no)
22 romantic - with a romantic relationship (binary: yes or no)
23 famrel - quality of family relationships (nominal: very_bad, bad, mediocre, good, excellent)
24 freetime - free time after school (nominal: very_low, low, mediocre, high, very_high)
25 goout - going out with friends (nominal: very_low, low, mediocre, high, very_high)
26 Dalc - workday alcohol consumption (nominal: very_low, low, mediocre, high, very_high)
27 Walc - weekend alcohol consumption (nominal: very_low, low, mediocre, high, very_high)
28 health - current health status (nominal: very_bad, bad, mediocre, good, excellent)
29 absences - number of school absences (nominal: none, one_to_three, four_to_six, seven_to_ten, more_than_ten)

Source: https://archive.ics.uci.edu/ml/datasets/student+performance# 

************************************************************************************
2.) The Bank Marketing data set

Instances:
- Each instance (line in the file) represents a client.

Label: 
- binary in {yes, no} indicating whether an individual has subscribed a term deposit.

Features: 
1 job : type of job (categorical: 'admin.','blue-collar','entrepreneur','housemaid','management','retired','self-employed','services','student','technician','unemployed','unknown')
2 marital : marital status (categorical: 'divorced','married','single','unknown'; note: 'divorced' means divorced or widowed)
3 education (categorical: 'basic.4y','basic.6y','basic.9y','high.school','illiterate','professional.course','university.degree','unknown')
4 default: has credit in default? (categorical: 'no','yes','unknown')
5 housing: has housing loan? (categorical: 'no','yes','unknown')
6 loan: has personal loan? (categorical: 'no','yes','unknown')
7 contact: contact communication type (categorical: 'cellular','telephone')
8 month: last contact month of year (categorical: 'jan', 'feb', 'mar', ..., 'nov', 'dec')
9 poutcome: outcome of the previous marketing campaign (categorical: 'failure','nonexistent','success')


Source: https://archive.ics.uci.edu/ml/datasets/bank+marketing


************************************************************************************
3.) The Obesity data set

Instances:
- Each instance (line in the file) represents a patient admitted to the hospital.

Label: 
- binary in {obese, not-obese} indicating whether a patient was identified as obese or not.

Features
1 Gender: patient's sex (binary: "Female" or "Male")	
2 family_history_with_overweight: patient's family history (binary: "yes" or "no")
3 FAVC: Frequent consumption of high caloric food (binary: "yes" or "no")
4 FCVC: Frequency of consumption of vegetables ( categorical: mid, high, low)	
5 NCP: Number of main meals ( categorical: high, very_high, mid, low)
6 CAEC: Consumption of food between meals (categorical: Sometimes, Frequently, Always, no)
7 SMOKE: patient's smoking (binary: "yes" or "no")	
8 CH2O: Consumption of water daily (categorical: mid, low, high)
9 SCC: Calories consumption monitoring (binary: "yes" or "no")
10 FAF: Physical activity frequency ( categorical: sedentary, low-activity, mid-activity, sedentary, high-activity)
11 TUE:  Time using technology devices ( categorical: good, mediocre, good, bad)
12 CALC: Consumption of alcohol ( categorical: Frequently, Sometimes, no, Always)
13 MTRANS: Transportation used (categorical: Public_Transportation, Automobile, Walking, Bike, Motorbike)


Source: https://archive.ics.uci.edu/ml/datasets/Estimation+of+obesity+levels+based+on+eating+habits+and+physical+condition+



************************************************************************************


