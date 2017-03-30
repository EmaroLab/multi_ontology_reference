import plotly
import plotly.graph_objs as go
import csv
import sys
import os

#This script generates the chart which compare the average execution times for each query between aMOR and OWL-API

# TIME_CSV_ID is the name of the time column in csv files;
# it is "time[ns]" for simple version of the test
# and "average[ns]" for the average extended version
#
TIME_CSV_ID = "average[ns]"

query = ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14"]

csv.field_size_limit(sys.maxsize)

##
# list_of_elements is the list of all csv file for a test
#
list_of_elements = []

##
# results_file_path is the path of the folder containing all file of the test (usually the folder is named with a timestamp)
#
results_file_path = sys.argv[1]


for subdirs, dirs, files in os.walk(results_file_path):
    for file in files:
        if "aMOR-" in os.path.realpath(file):
            list_of_elements.append(file)
        if "OWLAPI" in os.path.realpath(file):
            list_of_elements.append(file)

i = 0

while(i in range(len(list_of_elements)-1)):

    #these two arrays will contain queries's execution times
    aMORTime = []
    OWLAPITime = []

    charts_dir = results_file_path + "/" + list_of_elements[i].replace("aMOR-", "").replace("OWLAPI-", "").replace(".csv", "") + "/Charts"
    if not os.path.exists(charts_dir):
        os.makedirs(charts_dir)

    #here will be opened the aMOR and OWL-API csv files corresponding to a test on a specific ontology
    if("aMOR-" in list_of_elements[i]):
        aMORname = list_of_elements[i]
        aMORFile = open(results_file_path + "/" + list_of_elements[i].replace("aMOR-", "").replace(".csv", "") + "/" + list_of_elements[i], "r")
        OWLAPIFile = open(results_file_path + "/" + list_of_elements[i + 1].replace("OWLAPI-", "").replace(".csv", "") + "/" + list_of_elements[i + 1], "r")
    if("OWLAPI" in list_of_elements[i]):
        aMORname = list_of_elements[i+1]
        aMORFile = open(results_file_path + "/" + list_of_elements[i + 1].replace("aMOR-", "").replace(".csv", "") + "/" + list_of_elements[i + 1], "r")
        OWLAPIFile = open(results_file_path + "/" + list_of_elements[i].replace("OWLAPI-", "").replace(".csv", "") + "/" + list_of_elements[i], "r")

    OWLAPIReader = csv.DictReader(OWLAPIFile, delimiter=";")

    aMORReader = csv.DictReader(aMORFile, delimiter=";")

    for line in OWLAPIReader:
        OWLAPITime.append(line[TIME_CSV_ID])

    for line in aMORReader:
        aMORTime.append(line[TIME_CSV_ID])

    amorChart = go.Bar(
        x=query,
        y=aMORTime,
        name="aMOR",
        marker=dict(
            color="rgb(49,130,189)"
        )
    )

    owlAPIChart = go.Bar(
        x=query,
        y=OWLAPITime,
        name="OWL-API",
        marker=dict(
            color="rgb(204,50,50)"
        )
    )

    data = [amorChart, owlAPIChart]
    layout = go.Layout(
        xaxis=dict(tickangle=-45),
        barmode='group',
    )

    fig = go.Figure(data=data, layout=layout)
    plotly.offline.plot(fig, filename=charts_dir + '/average-time-Chart-' +
                                      aMORname.replace("aMOR-", "").replace(".csv", "").replace("uni", "").replace("dep", "") + '.html', auto_open=False)

    del aMORTime
    del OWLAPITime
    i += 2
