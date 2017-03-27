import plotly
import plotly.graph_objs as go
import csv
import sys
import os

#This script generates one chart which compares the standard deviation of all queries execution time between aMOR and OWL-API (based on 10 samples)
#and 14 charts, everyone with standard deviation of one query (based on 10 samples)

##
# QUERIES_NUMBER is the number of queries
# TEST_NUMBER is the number for each query is executed
#
QUERIES_NUMBER = 14
TEST_NUMBER = 10

#list_of_elements is the list of all csv file for a test
list_of_elements = []

# results_file_path is the path of the folder containing all file of the test (usually the folder is named with a timestamp)
results_file_path = sys.argv[1]

for subdirs, dirs, files in os.walk(results_file_path):
    for file in files:
        if "aMOR-" in os.path.realpath(file):
            list_of_elements.append(file)
        if "OWLAPI" in os.path.realpath(file):
            list_of_elements.append(file)

index = 0

while(index in range(len(list_of_elements)-1)):

    # these two arrays will contain queries's execution times
    aMORTime = []

    for _ in range(0, QUERIES_NUMBER):
        aMORTime.append([])

    OWLAPITime = []

    for _ in range(0, QUERIES_NUMBER):
        OWLAPITime.append([])

    csv.field_size_limit(sys.maxsize)
    if "aMOR-" in list_of_elements[index]:
        charts_dir = results_file_path + "/" + list_of_elements[index].replace("aMOR-", "").replace(".csv", "") + "/Charts"
    if "OWLAPI-" in list_of_elements[index]:
        charts_dir = results_file_path + "/" + list_of_elements[index].replace("OWLAPI-", "").replace(".csv", "") + "/Charts"
    if not os.path.exists(charts_dir):
        os.makedirs(charts_dir)
    single_query_std_dev_dir = charts_dir + "/single-query-stddev"
    if not os.path.exists(single_query_std_dev_dir):
        os.makedirs(single_query_std_dev_dir)
    ##
    # here will be opened the aMOR and OWL-API csv files corresponding to a test on a specific ontology
    #
    if "aMOR-" in list_of_elements[index]:
        aMORname = list_of_elements[index]
        aMORFile = open(results_file_path + "/" + list_of_elements[index].replace("aMOR-", "").replace(".csv", "") + "/" + list_of_elements[index], "r")
        OWLAPIFile = open(results_file_path + "/" + list_of_elements[index + 1].replace("OWLAPI-", "").replace(".csv", "") + "/" + list_of_elements[index + 1], "r")
    if "OWLAPI" in list_of_elements[index]:
        aMORname = list_of_elements[index+1]
        aMORFile = open(results_file_path + "/" + list_of_elements[index + 1].replace("aMOR-", "").replace(".csv", "") + "/" + list_of_elements[index + 1], "r")
        OWLAPIFile = open(results_file_path + "/" + list_of_elements[index].replace("OWLAPI-", "").replace(".csv", "") + "/" + list_of_elements[index], "r")

    OWLAPIReader = csv.DictReader(OWLAPIFile, delimiter=";")

    aMORReader = csv.DictReader(aMORFile, delimiter=";")

    i = 0
    j = 0

    for line in aMORReader:
        for k in range(1, TEST_NUMBER + 1):
            aMORTime[j].append(line["time" + str(k) + "[ns]"])
        j += 1

    for line in OWLAPIReader:
        for k in range(1, TEST_NUMBER + 1):
            OWLAPITime[i].append(line["time" + str(k) + "[ns]"])
        i += 1

    aMOR1trace = go.Box(
        y=aMORTime[0],
        name='aMOR query-1',
        boxpoints='all',
        jitter=0.3,
        marker=dict(
            color='rgb(49,130,189)',
        ),
    )

    aMOR2trace = go.Box(
        y=aMORTime[1],
        name='aMOR query-2',
        boxpoints='all',
        jitter=0.3,
        marker=dict(
            color='rgb(49,130,189)',
        ),
    )

    aMOR3trace = go.Box(
        y=aMORTime[2],
        name='aMOR query-3',
        boxpoints='all',
        jitter=0.3,
        marker=dict(
            color='rgb(49,130,189)',
        ),
    )

    aMOR4trace = go.Box(
        y=aMORTime[3],
        name='aMOR query-4',
        boxpoints='all',
        jitter=0.3,
        marker=dict(
            color='rgb(49,130,189)',
        ),
    )

    aMOR5trace = go.Box(
        y=aMORTime[4],
        name='aMOR query-5',
        boxpoints='all',
        jitter=0.3,
        marker=dict(
            color='rgb(49,130,189)',
        ),
    )

    aMOR6trace = go.Box(
        y=aMORTime[5],
        name='aMOR query-6',
        boxpoints='all',
        jitter=0.3,
        marker=dict(
            color='rgb(49,130,189)',
        ),
    )

    aMOR7trace = go.Box(
        y=aMORTime[6],
        name='aMOR query-7',
        boxpoints='all',
        jitter=0.3,
        marker=dict(
            color='rgb(49,130,189)',
        ),
    )

    aMOR8trace = go.Box(
        y=aMORTime[7],
        name='aMOR query-8',
        boxpoints='all',
        jitter=0.3,
        marker=dict(
            color='rgb(49,130,189)',
        ),
    )

    aMOR9trace = go.Box(
        y=aMORTime[8],
        name='aMOR query-9',
        boxpoints='all',
        jitter=0.3,
        marker=dict(
            color='rgb(49,130,189)',
        ),
    )

    aMOR10trace = go.Box(
        y=aMORTime[9],
        name='aMOR query-10',
        boxpoints='all',
        jitter=0.3,
        marker=dict(
            color='rgb(49,130,189)',
        ),
    )

    aMOR11trace = go.Box(
        y=aMORTime[10],
        name='aMOR query-11',
        boxpoints='all',
        jitter=0.3,
        marker=dict(
            color='rgb(49,130,189)',
        ),
    )

    aMOR12trace = go.Box(
        y=aMORTime[11],
        name='aMOR query-12',
        boxpoints='all',
        jitter=0.3,
        marker=dict(
            color='rgb(49,130,189)',
        ),
    )

    aMOR13trace = go.Box(
        y=aMORTime[12],
        name='aMOR query-13',
        boxpoints='all',
        jitter=0.3,
        marker=dict(
            color='rgb(49,130,189)',
        ),
    )

    aMOR14trace = go.Box(
        y=aMORTime[13],
        name='aMOR query-14',
        boxpoints='all',
        jitter=0.3,
        marker=dict(
            color='rgb(49,130,189)',
        ),
    )

    OWLAPI1trace = go.Box(
        y=OWLAPITime[0],
        name='OWL API query-1',
        boxpoints='all',
        jitter=0.3,
        marker=dict(
            color='rgb(204,50,50)',
        ),
    )

    OWLAPI2trace = go.Box(
        y=OWLAPITime[1],
        name='OWL API query-2',
        boxpoints='all',
        jitter=0.3,
        marker=dict(
            color='rgb(204,50,50)',
        ),
    )

    OWLAPI3trace = go.Box(
        y=OWLAPITime[2],
        name='OWL API query-3',
        boxpoints='all',
        jitter=0.3,
        marker=dict(
            color='rgb(204,50,50)',
        ),
    )

    OWLAPI4trace = go.Box(
        y=OWLAPITime[3],
        name='OWL API query-4',
        boxpoints='all',
        jitter=0.3,
        marker=dict(
            color='rgb(204,50,50)',
        ),
    )

    OWLAPI5trace = go.Box(
        y=OWLAPITime[4],
        name='OWL API query-5',
        boxpoints='all',
        jitter=0.3,
        marker=dict(
            color='rgb(204,50,50)',
        ),
    )

    OWLAPI6trace = go.Box(
        y=OWLAPITime[5],
        name='OWL API query-6',
        boxpoints='all',
        jitter=0.3,
        marker=dict(
            color='rgb(204,50,50)',
        ),
    )

    OWLAPI7trace = go.Box(
        y=OWLAPITime[6],
        name='OWL API query-7',
        boxpoints='all',
        jitter=0.3,
        marker=dict(
            color='rgb(204,50,50)',
        ),
    )

    OWLAPI8trace = go.Box(
        y=OWLAPITime[7],
        name='OWL API query-8',
        boxpoints='all',
        jitter=0.3,
        marker=dict(
            color='rgb(204,50,50)',
        ),
    )

    OWLAPI9trace = go.Box(
        y=OWLAPITime[8],
        name='OWL API query-9',
        boxpoints='all',
        jitter=0.3,
        marker=dict(
            color='rgb(204,50,50)',
        ),
    )

    OWLAPI10trace = go.Box(
        y=OWLAPITime[9],
        name='OWL API query-10',
        boxpoints='all',
        jitter=0.3,
        marker=dict(
            color='rgb(204,50,50)',
        ),
    )

    OWLAPI11trace = go.Box(
        y=OWLAPITime[10],
        name='OWL API query-11',
        boxpoints='all',
        jitter=0.3,
        marker=dict(
            color='rgb(204,50,50)',
        ),
    )

    OWLAPI12trace = go.Box(
        y=OWLAPITime[11],
        name='OWL API query-12',
        boxpoints='all',
        jitter=0.3,
        marker=dict(
            color='rgb(204,50,50)',
        ),
    )

    OWLAPI13trace = go.Box(
        y=OWLAPITime[12],
        name='OWL API query-13',
        boxpoints='all',
        jitter=0.3,
        marker=dict(
            color='rgb(204,50,50)',
        ),
    )

    OWLAPI14trace = go.Box(
        y=OWLAPITime[13],
        name='OWL API query-14',
        boxpoints='all',
        jitter=0.3,
        marker=dict(
            color='rgb(204,50,50)',
        ),
    )

    layout = go.Layout(
        width=1250,
        height=1100,
        yaxis=dict(
            title='Standard Deviation',
            zeroline=False
        ),
    )

    data = [aMOR1trace, OWLAPI1trace, aMOR2trace, OWLAPI2trace, aMOR3trace, OWLAPI3trace, aMOR4trace, OWLAPI4trace,
            aMOR5trace, OWLAPI5trace, aMOR6trace, OWLAPI6trace, aMOR7trace, OWLAPI7trace, aMOR8trace, OWLAPI8trace,
            aMOR9trace, OWLAPI9trace, aMOR10trace, OWLAPI10trace, aMOR11trace, OWLAPI11trace, aMOR12trace, OWLAPI12trace,
            aMOR13trace, OWLAPI13trace, aMOR14trace, OWLAPI14trace]

    data1 = [aMOR1trace, OWLAPI1trace]

    fig1 = go.Figure(data=data1, layout=layout)

    plotly.offline.plot(fig1, filename=single_query_std_dev_dir
                                    + '/query-1-Chart-'
                                    + aMORname.replace("aMOR-", "").replace(".csv", "").replace("uni", "").replace("dep", "")
                                    + '.html', auto_open=False)

    data2 = [aMOR2trace, OWLAPI2trace]

    fig2 = go.Figure(data=data2, layout=layout)

    plotly.offline.plot(fig2, filename=single_query_std_dev_dir
                                       + '/query-2-Chart-'
                                       + aMORname.replace("aMOR-", "").replace(".csv", "").replace("uni", "").replace("dep", "")
                                       + '.html', auto_open=False)

    data3 = [aMOR3trace, OWLAPI3trace]

    fig3 = go.Figure(data=data3, layout=layout)

    plotly.offline.plot(fig3, filename=single_query_std_dev_dir
                                       + '/query-3-Chart-'
                                       + aMORname.replace("aMOR-", "").replace(".csv", "").replace("uni", "").replace("dep", "")
                                       + '.html', auto_open=False)

    data4 = [aMOR4trace, OWLAPI4trace]

    fig4 = go.Figure(data=data4, layout=layout)

    plotly.offline.plot(fig4, filename=single_query_std_dev_dir
                                       + '/query-4-Chart-'
                                       + aMORname.replace("aMOR-", "").replace(".csv", "").replace("uni", "").replace("dep", "")
                                       + '.html', auto_open=False)

    data5 = [aMOR5trace, OWLAPI5trace]

    fig5 = go.Figure(data=data5, layout=layout)

    plotly.offline.plot(fig5, filename=single_query_std_dev_dir
                                       + '/query-5-Chart-'
                                       + aMORname.replace("aMOR-", "").replace(".csv", "").replace("uni", "").replace("dep", "")
                                       + '.html', auto_open=False)

    data6 = [aMOR6trace, OWLAPI6trace]

    fig6 = go.Figure(data=data6, layout=layout)

    plotly.offline.plot(fig6, filename=single_query_std_dev_dir
                                       + '/query-6-Chart-'
                                       + aMORname.replace("aMOR-", "").replace(".csv", "").replace("uni", "").replace("dep", "")
                                       + '.html', auto_open=False)

    data7 = [aMOR7trace, OWLAPI7trace]

    fig7 = go.Figure(data=data7, layout=layout)

    plotly.offline.plot(fig7, filename=single_query_std_dev_dir
                                       + '/query-7-Chart-'
                                       + aMORname.replace("aMOR-", "").replace(".csv", "").replace("uni", "").replace("dep", "")
                                       + '.html', auto_open=False)

    data8 = [aMOR8trace, OWLAPI8trace]

    fig8 = go.Figure(data=data8, layout=layout)

    plotly.offline.plot(fig8, filename=single_query_std_dev_dir
                                       + '/query-8-Chart-'
                                       + aMORname.replace("aMOR-", "").replace(".csv", "").replace("uni", "").replace("dep", "")
                                       + '.html', auto_open=False)

    data9 = [aMOR9trace, OWLAPI9trace]

    fig9 = go.Figure(data=data9, layout=layout)

    plotly.offline.plot(fig9, filename=single_query_std_dev_dir
                                       + '/query-9-Chart-'
                                       + aMORname.replace("aMOR-", "").replace(".csv", "").replace("uni", "").replace("dep", "")
                                       + '.html', auto_open=False)

    data10 = [aMOR10trace, OWLAPI10trace]

    fig10 = go.Figure(data=data10, layout=layout)

    plotly.offline.plot(fig10, filename=single_query_std_dev_dir
                                       + '/query-10-Chart-'
                                       + aMORname.replace("aMOR-", "").replace(".csv", "").replace("uni", "").replace("dep", "")
                                       + '.html', auto_open=False)

    data11 = [aMOR11trace, OWLAPI11trace]

    fig11 = go.Figure(data=data11, layout=layout)

    plotly.offline.plot(fig11, filename=single_query_std_dev_dir
                                       + '/query-11-Chart-'
                                       + aMORname.replace("aMOR-", "").replace(".csv", "").replace("uni", "").replace("dep", "")
                                       + '.html', auto_open=False)

    data12 = [aMOR12trace, OWLAPI12trace]

    fig12 = go.Figure(data=data12, layout=layout)

    plotly.offline.plot(fig12, filename=single_query_std_dev_dir
                                       + '/query-12-Chart-'
                                       + aMORname.replace("aMOR-", "").replace(".csv", "").replace("uni", "").replace("dep", "")
                                       + '.html', auto_open=False)

    data13 = [aMOR13trace, OWLAPI13trace]

    fig13 = go.Figure(data=data13, layout=layout)

    plotly.offline.plot(fig13, filename=single_query_std_dev_dir
                                       + '/query-13-Chart-'
                                       + aMORname.replace("aMOR-", "").replace(".csv", "").replace("uni", "").replace("dep", "")
                                       + '.html', auto_open=False)
    data14 = [aMOR14trace, OWLAPI14trace]

    fig14 = go.Figure(data=data14, layout=layout)

    plotly.offline.plot(fig14, filename=single_query_std_dev_dir
                                       + '/query-14-Chart-'
                                       + aMORname.replace("aMOR-", "").replace(".csv", "").replace("uni", "").replace("dep", "")
                                       + '.html', auto_open=False)

    fig = go.Figure(data=data, layout=layout)
    plotly.offline.plot(fig, filename=charts_dir + '/total-stddev-Chart-'
                                       + aMORname.replace("aMOR-", "").replace(".csv", "").replace("uni", "").replace("dep", "")
                                       + '.html', auto_open=False)

    del aMORTime
    del OWLAPITime
    index += 2
