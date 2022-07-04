//download item sales info from amazon seller central orders > order reports
//change date range to largest available, request
//copy text file output to excel spreadsheet name it amazon_orig.xlsx
//put amazon_orig.xlsx in same folder as application
//run batch_script_to_run_the_thing.bat

import java.util.ArrayList;
import FileIO.*;
import java.io.PrintWriter;
import StatMethods.StatMethods;
import java.lang.Math;
import java.util.Date; //to use current date to determine the weekly ranges that you will get
import java.time.*;  
import java.util.Calendar;

public class AmazonMessingTake4_2 {
    public static ArrayList < ArrayList <Integer> > weeklyDate2DArr(int[] startDate, int[] endDate){
        ArrayList <Integer> month1 = new ArrayList();
        ArrayList <Integer> day1 = new ArrayList();
        
        ArrayList <Integer> month2 = new ArrayList();
        ArrayList <Integer> day2 = new ArrayList();
        //days per month (excluding leap year)
        int[] monthDays = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        
        int mon = startDate[0];
        int day = startDate[1];
        boolean yup = true;
        int willy = 0;
        while(yup){
            if(mon < endDate[0]){ //previous month
                if(day <= (monthDays[mon - 1])){
                    day1.add(day);
                    month1.add(mon);
                    day = day + 7;
                }
                else{
                    day = day%(monthDays[mon - 1]);
                    mon++;
                }
            }
            else if(mon == endDate[0]){
                if(day <= endDate[1]){
                    day1.add(day);
                    month1.add(mon);
                    day = day + 7;
                }
                else{
                    yup = false;
                }
            }
            else{
                yup = false;
            }
        }
        
        System.out.println(month1);
        System.out.println(day1);
        
        //2D array to return month(index 0) and day(index 1)
        ArrayList < ArrayList <Integer> > month1AndDay1 = new ArrayList();
        month1AndDay1.add(month1);
        month1AndDay1.add(day1);
        
        return month1AndDay1;
        
    }
    
    //Amazon date to array method
    public static int[] parseAmzDateArr(String fromSheet){
        String[] dateSplitStr = fromSheet.split("-|T");
        int[] dateSplitInt = new int[3];
        
        for(int w = 0; w < 3; w++){
            dateSplitInt[w] = Integer.parseInt(dateSplitStr[w].trim());
        }
        
        return dateSplitInt;
    }
    
    //method for returning array of all item #s for all of the item arrays for each week
    public static ArrayList <String> itemMasterList(ArrayList < ArrayList <String> > items2D){
        ArrayList <String> itemList = new ArrayList();
        
        for(int a = 0; a < items2D.size(); a++){
            
            for(int b = 0; b < items2D.get(a).size(); b++){
                
                int foundAMatch = 0;
                for(int c = 0; c < itemList.size(); c++){
                    if(items2D.get(a).get(b).trim().equals(itemList.get(c).trim())){
                        foundAMatch++;
                    }
                }
                if(foundAMatch == 0){
                    itemList.add(items2D.get(a).get(b).trim());
                }
            }
        }
        
        return itemList;
    }
    
    //tally all of the sales items
    public static ArrayList < ArrayList < Integer > > masterCounts(ArrayList <String> masterList, ArrayList < ArrayList <String> > items2D, ArrayList < ArrayList <Integer> > counts2D){
        ArrayList < ArrayList < Integer > > theMasterCountsArray = new ArrayList();
        
        for(int a = 0; a < masterList.size(); a++){
            
            //1D ArrayList for storing the horizontal counts for each item to add 1 by 1 to theMasterCountsArray
            ArrayList <Integer> oneItemAllWeeksCounts = new ArrayList();
            for(int b = 0; b < items2D.size(); b++){
                
                //counter for if you do not find item from masterList add 0
                int foundIt = 0;
                for(int c = 0; c < items2D.get(b).size(); c++){
                    if(masterList.get(a).equals(items2D.get(b).get(c))){
                        oneItemAllWeeksCounts.add(counts2D.get(b).get(c));
                        foundIt++;
                    }
                }
                if(foundIt == 0){
                    oneItemAllWeeksCounts.add(0);
                }
            }
            theMasterCountsArray.add(oneItemAllWeeksCounts);
        }
        return theMasterCountsArray;
    }
    
    //print totals/averages with headers and sideHeaders 
    public static void fileOutput2DArrayListHeadersSideHeaders(ArrayList < ArrayList < Integer > > int2D, ArrayList <String> headers, ArrayList <String> sideHeaders, String filename){
        PrintWriter out = null;
        
        try{
            out = new PrintWriter(filename);
        }
        catch(Exception e){
            System.out.printf("Error: failed to open file %s.\n", filename);
            System.exit(0);
        }
        
        String headersWithCommas = "***"; //*** for cell (0,0)
        for(int i = 0; i < headers.size(); i++){
            headersWithCommas = headersWithCommas + "," + headers.get(i);    
        }
        
        out.println(headersWithCommas);
        
        //for loop for printing each row after headers (item #,count1,count2,...)    
        for(int a = 0; a < sideHeaders.size(); a++){
            String eachRow = sideHeaders.get(a);
            for(int b = 0; b < int2D.get(a).size(); b++){
                eachRow = eachRow + "," + int2D.get(a).get(b);
            }
            out.println(eachRow);
        }
        
        out.close();
        
    }
    
    //method for creating string for week beginning header dates
    public static ArrayList <String> weekHeaders(ArrayList < ArrayList <Integer> > monthDayStartDates){
        ArrayList <String> weekStartStringHeaders = new ArrayList();
        
        for(int a = 0; a < monthDayStartDates.get(0).size(); a++){
            String oneMonthDay = Integer.toString(monthDayStartDates.get(0).get(a)) + "/" + Integer.toString(monthDayStartDates.get(1).get(a));
            weekStartStringHeaders.add(oneMonthDay);
        }
        
        return weekStartStringHeaders;
       
    }
    
    //method for adding column for average of each row
    public static ArrayList < ArrayList < Integer > > int2DPlusOneColAvg(ArrayList < ArrayList < Integer > > int2D){
        //assigning all of the original int2D array to new int2DPlusColAvg array, then will add new column in for loop
        ArrayList < ArrayList < Integer > > int2DPlusColAvg = int2D;
        for(int a = 0; a < int2D.size(); a++){
            double avgForRow = getAveragePerRowArg2DArrayListIntArr(int2DPlusColAvg, a, false);
            //need to round if you want to return an int ArrayList
            double avgForRowOnTheWayToInt = Math.round(avgForRow);
            int avgForRowRoundedToInt = (int) avgForRowOnTheWayToInt;
            int2DPlusColAvg.get(a).add(avgForRowRoundedToInt);
        }
        return int2DPlusColAvg;
    }
    
    //method takes int array argument, not string array
    public static double getAveragePerRowArg2DArrayListIntArr(ArrayList < ArrayList < Integer > > int2D, int whichRow, boolean rowLabel){
        double total = 0;
        int start = 0;
        int wall = int2D.get(whichRow).size();

        if(rowLabel){
            start = 1;
            wall = wall - 1;
        }
        
        for(int a = start; a < wall; a++){
            total = total + int2D.get(whichRow).get(a);
        }
        
        double average = total/(wall);
        return average;
    }

    public static void main(String[] args) {
        //import spreadsheet 
        //String [][] amz = FileIO.fileTo2DArr("AmazonSpreadsheetNoBlanks.csv");
        //this csv file is created by running the python file to select the 4 columns that you want for this application
        String [][] amz = FileIO.fileTo2DArr("just_the_4_cols_you_want_no_index.csv");
        FileIO.print2DArray(amz);
        
        int[] monthDays = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        
        Date date = new Date();
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        int year  = localDate.getYear();
        int month = localDate.getMonthValue();
        int day   = localDate.getDayOfMonth();
        
        //just printing
        System.out.println(month);
        System.out.println(day);
        System.out.println(year);
        
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        System.out.println(dayOfWeek);
        
        //declare start date and end date, actual dates are determined by getting the current date and doing the calculation
        int[] startDate = {0, 0, 0};
        int[] endDate = {0, 0, 0};
        
        //ok so for the end date you're looking for Saturday
        //need to pay attention to months and subtract the day # from today
        int endDateDay = day - dayOfWeek; //get to Saturday
        int endDateMonth = month;
        int endDateYear = year;
        
        System.out.println(endDateMonth);
        System.out.println(endDateDay);
        System.out.println(endDateYear);
        
        //check if endDateDay is less than 1 so goes into the previous month
        if(endDateDay < 1){
            endDateMonth = endDateMonth - 1;
            if (endDateMonth == 0){ //going into previous year
                endDateMonth = 12;
                endDateYear = endDateYear - 1;
            }
            //calculate day in the previous month as the amount of days 
            //minus the amount that was left over from going backwards in time from the previous month
            endDateDay = endDateDay + monthDays[endDateMonth - 1];
        }
        //put the day, month, year info into a tuple
        endDate[0] = endDateMonth;
        endDate[1] = endDateDay;
        endDate[2] = endDateYear;
        
        //calculate start date
        int startDateDay = day - dayOfWeek + 1 - 28;
        int startDateMonth = month;
        int startDateYear = year;
        if(startDateDay < 1){
            startDateMonth = startDateMonth - 1;
            if(startDateMonth == 0){
                startDateMonth = 12;
                startDateYear = startDateYear - 1;
            }
            startDateDay = startDateDay + monthDays[startDateMonth - 1];
        }
        startDate[0] = startDateMonth;
        startDate[1] = startDateDay;
        startDate[2] = startDateYear;
        
        //below generates the 2 2D arrays of month/day that are needed to loop through later
        //************************************
        ArrayList < ArrayList <Integer> > monthDayStartDates = weeklyDate2DArr(startDate, endDate);
        System.out.println("Weekly Start Dates");
        System.out.println(monthDayStartDates);
        
        int[] weekEndDate = new int[3];
        
        if((startDate[1] + 6) <= monthDays[startDate[0] - 1]){
            weekEndDate[0] = startDate[0];
            weekEndDate[1] = startDate[1] + 6;
            weekEndDate[2] = startDate[2];
        }
        else{
            weekEndDate[0] = startDate[0] + 1;
            weekEndDate[1] = (startDate[1] + 6)%monthDays[startDate[0] - 1];
            weekEndDate[2] = startDate[2];
        }
        
        ArrayList < ArrayList <Integer> > monthDayWeekEndDates = weeklyDate2DArr(weekEndDate, endDate);
        System.out.println("Weekly End Dates");
        System.out.println(monthDayWeekEndDates);
        
        //********************************************
        
        //here are the 2D Arrays that you will repeatedly push the weekly counts into
        ArrayList < ArrayList <String> > items2D = new ArrayList();
        ArrayList < ArrayList <Integer> > counts2D = new ArrayList();
        
        //looping through week by week
        //create month and day variables for conditional comparison
        System.out.println("monthDayWeekEndDates.get(0).size()");
        System.out.println(monthDayWeekEndDates.get(0).size());
        for(int q = 0; q < monthDayWeekEndDates.get(0).size(); q++){
            //monthDayStartDates is a 2D Array (month, day)
            int monthToCheckStart = monthDayStartDates.get(0).get(q);
            int dayToCheckStart = monthDayStartDates.get(1).get(q);
            int monthToCheckEnd = monthDayWeekEndDates.get(0).get(q);
            int dayToCheckEnd = monthDayWeekEndDates.get(1).get(q);
            //placing declaration of items and counts ArrayLists here 
            //resets for every new q
            ArrayList <String> items = new ArrayList <String>();
            ArrayList <Integer> counts = new ArrayList <Integer>();
            
            //for loop for all dates on spreadsheet
            //changed r = 0 to r = 1 below to not parse header
            for(int r = 1; r < amz.length; r++){
                //parseAmzDateArr here to get values to check
                int[] yearMonthDay = parseAmzDateArr(amz[r][1]);
                
                //logic for checking month 
                if(monthToCheckStart == monthToCheckEnd){ //start in same month
                    if(yearMonthDay[1] == monthToCheckStart){
                        if(yearMonthDay[2] >= dayToCheckStart && yearMonthDay[2] <= dayToCheckEnd){
                            //the adding items and counts process
                            int match = 0;
            
                            for(int s = 0; s < items.size(); s++){
                                //count matches for each product item found in file
                                if(amz[r][2].equals(items.get(s))){
                                    match++;
                                    counts.set(s, counts.get(s) + Integer.parseInt(amz[r][3])); 
                                }
                            }

                            if(match == 0){
                               items.add(amz[r][2]);
                               counts.add(Integer.parseInt(amz[r][3]));
                            }
                        }
                    }
                }
                else if(monthToCheckStart == (monthToCheckEnd - 1)){ //if start is in previous month
                    if(yearMonthDay[1] == monthToCheckStart){
                        if(yearMonthDay[2] >= dayToCheckStart && yearMonthDay[2] <= monthDays[yearMonthDay[1] - 1]){
                            //the adding items and counts process
                            int match = 0;
            
                            for(int s = 0; s < items.size(); s++){

                                if(amz[r][2].equals(items.get(s))){
                                    match++;
                                    counts.set(s, counts.get(s) + Integer.parseInt(amz[r][3])); 
                                }
                            }

                            if(match == 0){
                               items.add(amz[r][2]);
                               counts.add(Integer.parseInt(amz[r][3]));
                            }
                        }
                    }
                    else if(yearMonthDay[1] == monthToCheckEnd){ //item sale is in end month range
                        if(yearMonthDay[2] <= dayToCheckEnd){
                            //the adding items and counts process
                            int match = 0;
            
                            for(int s = 0; s < items.size(); s++){
                                if(amz[r][2].equals(items.get(s))){
                                    match++;
                                    counts.set(s, counts.get(s) + Integer.parseInt(amz[r][3])); 
                                }
                            }

                            if(match == 0){
                               items.add(amz[r][2]);
                               counts.add(Integer.parseInt(amz[r][3]));
                            }
                        }
                    }
                }
                
            }
            //adding items and counts to 2D arrays before going to next q (week)
            items2D.add(items);
            counts2D.add(counts);
        }
        
        System.out.println("items2D");
        System.out.println(items2D.size());
        System.out.println(items2D);
        System.out.println("counts2D");
        System.out.println(counts2D.size());
        System.out.println(counts2D);
        
        ArrayList <String> masterList = itemMasterList(items2D);
        System.out.println("The master list");
        System.out.println(masterList.size());
        System.out.println(masterList);
        
        //organized ArrayList by master item ordered list to use to print out organized weekly item spreadsheet
        ArrayList < ArrayList < Integer > > organizedCounts2D = masterCounts(masterList, items2D, counts2D);
        System.out.println(organizedCounts2D);
        
        //create ArrayList of weekly headers as strings
        ArrayList <String> weekStartStringHeaders = weekHeaders(monthDayStartDates);
        System.out.println(weekStartStringHeaders);
        
        //print to file
        fileOutput2DArrayListHeadersSideHeaders(organizedCounts2D, weekStartStringHeaders, masterList, "Amz_Weekly.csv");
        
        //get averages
        double avgEx = getAveragePerRowArg2DArrayListIntArr(organizedCounts2D, 5, false);
        System.out.println(avgEx);
        
        ArrayList < ArrayList < Integer > > organizedCounts2DAddColAvg = int2DPlusOneColAvg(organizedCounts2D);
        System.out.println("With Average");
        System.out.println(organizedCounts2DAddColAvg);
        
        //add column header for average
        ArrayList <String> weekStartStringHeadersPlusAvg = weekStartStringHeaders;
        weekStartStringHeadersPlusAvg.add("Average");
        fileOutput2DArrayListHeadersSideHeaders(organizedCounts2DAddColAvg, weekStartStringHeadersPlusAvg, masterList, "Amz_Weekly_With_Avg.csv");
        
    }
    
}
