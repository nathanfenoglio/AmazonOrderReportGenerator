import pandas as pd # pip3 install pandas ALSO pip3 install openpyxl

# change spreadsheet name to whatever you are titling the master spreadsheet
# titling it amazonster.csv
dataframe_the_past = pd.read_csv('amazonster.csv')

# axis=1 signifies that you're dropping a column, axis=0 would signify to drop a row
# drop both the Average and 4 Week Avg from the master amazonster.csv running totals spreadsheet
dataframe_the_past = dataframe_the_past.drop('Average', axis=1) 
dataframe_the_past = dataframe_the_past.drop('4 Week Avg', axis=1) 
num_cols_in_orig_spreadsheet = len(dataframe_the_past.iloc[0])

# get the_present 4 week totals spreadsheet
dataframe_the_present = pd.read_csv('Amz_Weekly_With_Avg.csv')

# drop the Average column of the_present spreadsheet, will recalculate later on the new spreadsheet
dataframe_the_present_no_avg = dataframe_the_present.drop('Average', axis=1)

# get a list of the column headers
header_list_the_present = list(dataframe_the_present_no_avg.columns.values)
header_list_the_past = list(dataframe_the_past.columns.values)

# just printing the dates before converting
print('header_list_the_past')
print(header_list_the_past)
print('header_list_the_present')
print(header_list_the_present)

# it seems to make a distinction between like 6/12 and 12-Jun which screws up finding the duplicate columns
# so the present Amz_Weekly_With_Avg.csv has header dates like 6/12 6/19 6/26 etc
# and the past amazonster.csv has header dates like 5-Jun 12-Jun 19-Jun 26-Jun etc
# the date conversion stuff below seems to have fixed the sometimes different date formats that come in from the different spreadsheets
excel_month_abbr = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']
for one_date in header_list_the_past:
    # string.find returns index of 1st occurence of what it's looking for or -1 if not found
    # using the 12-Jun type format to compare, converting 6/12 type format to 12-Jun type format
    if one_date.find('-') != -1:  
        pass
    if one_date.find('/') != -1: # convert to 12-Jun type format for accurate comparison
        month_day_list = one_date.split('/')
        converted_date_str = month_day_list[1] + "-" + excel_month_abbr[int(month_day_list[0]) - 1]
        # change the headers to the new formatted date
        header_list_the_past[index] = converted_date_str
        dataframe_the_past = dataframe_the_past.rename(columns={one_date : converted_date_str})

for index, one_date in enumerate(header_list_the_present):
    print(one_date)
    # string.find returns index of 1st occurence of what it's looking for or -1 if not found
    # using the 12-Jun type format to compare, converting 6/12 type format to 12-Jun type format    
    if one_date.find('-') != -1: 
        pass
    if one_date.find('/') != -1: # convert to 12-Jun type format for accurate comparison
        month_day_list = one_date.split('/')
        converted_date_str = month_day_list[1] + "-" + excel_month_abbr[int(month_day_list[0]) - 1]
        header_list_the_present[index] = converted_date_str
        dataframe_the_present_no_avg = dataframe_the_present_no_avg.rename(columns={one_date : converted_date_str})

# just printing the converted dates
print('header_list_the_present')
print(header_list_the_present)
print('header_list_the_past')
print(header_list_the_past)

# get all of the old columns you already had to build new master spreadsheet
dataframe_new_master = dataframe_the_past 

# check from right end of the_present header list until you find a duplicate header
# and save each new column in a 2d list to add to the master spreadsheet
# the columns to add will be in reverse time order from what you want later since moving right to left here 
cols_to_add = []
col_headers_to_add = []
for col_name_the_present in reversed(list(header_list_the_present)):
    already_have_this_col = False
    for col_name_the_past in reversed(list(header_list_the_past)):
        if col_name_the_present == col_name_the_past:
            already_have_this_col = True
            break
    
    if not(already_have_this_col):
        cols_to_add.append(dataframe_the_present_no_avg[[col_name_the_present]])
        col_headers_to_add.append(col_name_the_present)

# add column of all 0s for each new week column that you will be adding
for col_name in reversed(col_headers_to_add):
    dataframe_new_master[col_name] = [0] * len(dataframe_the_past) 

# then add the columns to the new master spreadsheet  
# checking to match each row from the new columns to the correct row of the old sheet
for col_to_add_index in range(len(cols_to_add)): # for each column that you are adding
    for item_to_add_index in range(len(cols_to_add[col_to_add_index])): # for each row in the column that you are adding        
        item_num_to_find = dataframe_the_present_no_avg.loc[item_to_add_index, '***'] # *** signifies the item # column        
        # find item # location in original spreadsheet
        already_had_item = False
        for orig_item_index in range(len(dataframe_new_master)): # check each row in original spreadsheet
            item_num_orig_spreadsheet = dataframe_new_master.loc[orig_item_index, '***']
            if item_num_to_find == item_num_orig_spreadsheet:
                already_had_item = True
                # put item count in the master spreadsheet at orig_item_index location
                # a little extra math to reverse the order of the columns that you are adding since they were added in reverse order
                current_col = num_cols_in_orig_spreadsheet + len(cols_to_add) - col_to_add_index - 1 
                dataframe_new_master.iloc[orig_item_index, current_col] = cols_to_add[col_to_add_index].iloc[item_to_add_index]
                break

        # if there is a new item # that is not already in the old sheet
        # need to add that item # at the bottom and add 0s for all of the old columns that didn't have that item for that week
        if not(already_had_item):
            # a little extra math to reverse the order of the columns that you are adding since they were added in reverse order
            current_col = num_cols_in_orig_spreadsheet + len(cols_to_add) - col_to_add_index - 1
            row_to_add = []
            row_to_add.append(item_num_to_find) # add the new item # label side header
            for i in range(1, current_col + 1):
                if i == current_col:
                    # seemingly convoluted way to get just the value of the row and column that you are needing to add 
                    row_to_add.append(cols_to_add[col_to_add_index].iloc[item_to_add_index].values[0])
                else: # not the week that has sales for this new item, so append 0
                    row_to_add.append(0)

            # add row at very bottom of spreadsheet
            dataframe_new_master.loc[len(dataframe_new_master.index)] = row_to_add

# add total average and 4 week average to each row 
dataframe_new_master['Average'] = [0] * len(dataframe_new_master)
# keeping track of where the last actual week sales info is for for loops
last_week_col_plus_one = len(dataframe_new_master.iloc[0])
dataframe_new_master['4 Week Avg'] = [0] * len(dataframe_new_master)
for i in range(len(dataframe_new_master)):
    row_total = 0
    last_four_weeks_total = 0
    for j in range(1, last_week_col_plus_one):
        row_total += dataframe_new_master.iloc[i, j]
        if j >= last_week_col_plus_one - 1 - 4:
            last_four_weeks_total += dataframe_new_master.iloc[i, j]

    dataframe_new_master.iloc[i, last_week_col_plus_one - 1] = round(row_total / (last_week_col_plus_one - 2)) 
    dataframe_new_master.iloc[i, last_week_col_plus_one - 1 + 1] = round(last_four_weeks_total / 4) 

print(dataframe_new_master)
dataframe_new_master.to_csv('amazonster.csv', index=False)


