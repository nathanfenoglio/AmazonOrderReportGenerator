import pandas as pd # pip3 install pandas ALSO pip3 install openpyxl

dataframe = pd.read_excel('amazon_orig.xlsx') # xlsx file not csv file
print(dataframe)

# NEED TO DITCH THE ROWS THAT HAVE BLANK CELLS (SOMETIMES THE DATA COMES LIKE THAT)
dataframe = dataframe.dropna(subset=['sku'])

# only keep the data from the columns that you care about 
# order-id purchase-date sku quantity-purchased 
dataframe = dataframe[['order-id', 'purchase-date', 'sku', 'quantity-purchased']]
print(dataframe)

# HMMMM I'M GETTING NUMBERFORMATEXCEPTION IN THE JAVA PROGRAM BECAUSE quantity-purchased IS READ IN LIKE 1.0 INSTEAD OF 1 I GUESS
# WHEN I LOOK AT THE just_the_4_cols_you_want_no_index.csv FILE IT LOOKS LIKE INTS THOUGH
 
# could maybe do all that you did with the java project here easily with the pivot table example...

# save to csv file with only the columns that you want
dataframe.to_csv('just_the_4_cols_you_want_no_index.csv', index=False)
 
