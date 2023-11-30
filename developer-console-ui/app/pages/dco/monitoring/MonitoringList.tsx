import { Box, Button, Table, Toast } from "@dco/sdv-ui"
import { useStoreActions, useStoreState } from "easy-peasy"
import { useEffect, useState } from "react"
import Pagination from "../../shared/paginationTable"
import BoxToast from "../../../components/layout/boxToast"
import {
  getReleaseData,
  numberOfReleases,
  releaseManagementRowData
} from "../../../services/functionReleaseManagement.services"
import GraphModal from "./graphModal"


export function Selectedscenario() {
  return useStoreState((state: any) => state.selectedscenario)
}


const MonitoringList = ({ path }: any) => {
  const setCount = useStoreActions((actions: any) => actions.setCount)
  const [isToastOpenScenario, setToastOpenScenario] = useState(false)
  const [successMsgScenario, setSuccessMsgScenario] = useState('')
  const [currentPage, setCurrentPage] = useState(1)
  const [showGraphPopup, setShowGraphPopup] = useState(false)
  const [pageData, setPageData] = useState({
    rowData: [],
    isLoading: false,
    totalPages: 0,
    totalReleases: 0,
  })

  useEffect(() => {
    setPageData((prevState) => ({
      ...prevState,
      rowData: [],
      isLoading: true,
    }))
    // getReleaseData().then((info) => {
    fetchMockSuccessReleases().then((info) => {
      const filteredReleases = info?.filter((item: any) => item.releaseStatus == 'READY_FOR_RELEASE')
      setPageData({
        isLoading: false,
        rowData: releaseManagementRowData(filteredReleases),
        totalPages: 1,
        totalReleases: numberOfReleases(filteredReleases),
      })
      setCount(numberOfReleases(filteredReleases));
    })
  }, [])

  const fetchMockSuccessReleases = async () => {
    fetch('../app/data/releases.json')
      .then((res) => res.json())
      .then((result) => {result; console.log('result from file ', result)})
      
      .catch(error => console.log('file not found'))
  }

  const columns = [
    {
      Header: 'Release ID',
      accessor: 'releaseId',
    },
    {
      Header: 'Release Date',
      accessor: 'releaseDate',
    },
    {
      Header: '',
      accessor: 'menu',
      formatter: (value: any, cell: any) => {
        return (<Button name={"show-graph"} onClick={() => { setShowGraphPopup(true); showGraph(cell.row.values.releaseId) }}>Analytics View</Button>)
      }
    },
  ]

  const showGraph = (releaseId: string) => {
    const graphRes = ''
    if (releaseId) {
      fetch(`http://localhost:8086/api/${releaseId}`)
        .then(res => res.json())
        .then(res => {
          console.log('ghraph response ==', res)
        })
        .catch(error =>
          console.log('error in fetching graphs =', error)
        )
    }
    <GraphModal releaseId={releaseId} show={showGraphPopup} onClose={setShowGraphPopup} graph={graphRes} />
  }

  return (
    <>
      <Table data-testid="table" columns={columns}
        data={pageData.rowData} initialSortBy={[
          {
            id: 'lastUpdated',
            desc: true
          }
        ]}
        noDataMessage='No Rows To Show'
      />
      <Box align='right' padding='small'>
        <Pagination totalRows={pageData.totalReleases} pageChangeHandler={setCurrentPage} rowsPerPage={10} />
      </Box>
      {/* Toast message for deleting a scenario */}
      {<Toast show={isToastOpenScenario}>
        <div>
          <BoxToast toastMsg={successMsgScenario} />
        </div>
      </Toast>
      }
    </>
  )
}
export default MonitoringList