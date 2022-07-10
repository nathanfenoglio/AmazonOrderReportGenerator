import pandas as pd # pip3 install pandas ALSO pip3 install openpyxl

dataframe = pd.read_excel('amazon_orig.xlsx') # xlsx file not csv file
print(dataframe)

# only keep the data from the columns that you care about 
# order-id purchase-date sku quantity-purchased 
dataframe = dataframe[['order-id', 'purchase-date', 'sku', 'quantity-purchased']]
print(dataframe)

# could maybe do all that you did with the java project here easily with the pivot table example...

# save to csv file with only the columns that you want
dataframe.to_csv('just_the_4_cols_you_want_no_index.csv', index=False)
 
