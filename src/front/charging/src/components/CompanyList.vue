<script setup>
import axios from 'axios'
import {onMounted, ref} from 'vue'

const newCompany = ref({
  name: "",
  parentCompanyId: null
})
//companyId to template
const newStations = ref({})

const companies = ref({})

const editedStations = ref(new Set())
const editedCompanies = ref(new Set())

const companyStationsView = ref({
  companyId: null,
  stations: []
})
const stationsIdRadiusView = ref({
  latitude: null,
  longitude: null,
  radiusKm: null
})

onMounted(async () => {
  try {
    const response = await axios.get('http://localhost:8080/company')

    for (const company of response.data) {
      putCompanyToRenderedSet(company)
    }
  } catch (error) {
    window.alert(error.response.data.message ?? error.response.data)
  }
})


async function addCompany() {
  try {
    const response = await axios.post('http://localhost:8080/company', newCompany.value)

    putCompanyToRenderedSet(response.data)
  } catch (error) {
    window.alert(error.response.data.message ?? error.response.data)
  }
}

async function addStation(companyId) {
  try {
    const response = await axios.post('http://localhost:8080/station', newStations.value[companyId])
    const station = response.data

    companies.value[companyId]
        .stations[station.id] = station
  } catch (error) {
    window.alert(error.response.data.message ?? error.response.data)
  }
}

async function updateCompany(id) {
  try {
    const response = await axios.put(`http://localhost:8080/company`, companies.value[id])
    const company = response.data
    company.stations = companies.value[id].stations

    companies.value[id] = company

    editedCompanies.value.delete(id)
  } catch (error) {
    window.alert(error.response.data.message ?? error.response.data)
  }
}

async function updateStation(companyId, id) {
  try {
    const response = await axios.put(`http://localhost:8080/station`, companies.value[companyId].stations[id])

    companies.value[companyId].stations[id] = response.data

    editedStations.value.delete(id)
  } catch (error) {
    window.alert(error.response.data.message ?? error.response.data)
  }
}

async function deleteCompany(id) {
  try {
    await axios.delete(`http://localhost:8080/company/${id}`)

    delete companies.value[id]
    delete newStations.value[id]
  } catch (error) {
    window.alert(error.response.data.message ?? error.response.data)
  }
}

async function deleteStation(companyId, stationId) {
  try {
    await axios.delete(`http://localhost:8080/station/${stationId}`)
    const company = companies.value[companyId]

    delete company.stations[stationId]
  } catch (error) {
    window.alert(error.response.data.message ?? error.response.data)
  }
}

async function searchCompanyStations() {
  try {
    const response = await axios.get(`http://localhost:8080/company/${companyStationsView.value.companyId}/station`)

    companyStationsView.value.stations = response.data
  } catch (error) {
    window.alert(error.response.data.message ?? error.response.data)
  }
}

async function searchStationsInRadius() {
  try {
    const {latitude, longitude, radiusKm} = stationsIdRadiusView.value

    const response = await axios.get(`http://localhost:8080/station`, {params: {latitude, longitude, radiusKm}})

    stationsIdRadiusView.value.stations = response.data
  } catch (error) {
    window.alert(error.response.data.message ?? error.response.data)
  }
}

function putCompanyToRenderedSet(company) {
  company.stations = {}
  companies.value[company.id] = company
  newStations.value[company.id] = {
    name: "",
    latitude: null,
    longitude: null,
    companyId: company.id
  }
}

</script>

<template>
  <h1>
    Edit companies and stations
  </h1>
  <div v-for="{id : companyId, name, parentCompany, stations} in companies">
    <div class="company-entry">
      <div class="company-data" v-if="!editedCompanies.has(companyId)">
        {{ companyId }} {{ name }} {{ parentCompany?.id }}
        <button @click="editedCompanies.add(companyId)">
          Edit Company
        </button>
      </div>
      <div class="company-data" v-else>
        <input v-model="companies[companyId].name" placeholder="Name">
<!--        <input v-model="companies[companyId].parentCompanyId" type="number" placeholder="Parent Id">-->
        <button @click="updateCompany(companyId)">
          Submit Company
        </button>
      </div>
      <button @click="deleteCompany(companyId)">
        Delete Company
      </button>
    </div>

    <div class="stations">
      <div v-for="{id : stationId, latitude, longitude, name} in stations" class="station-entry">
        <div class="station-data" v-if="!editedStations.has(stationId)">
          {{ stationId }} {{ name }} {{ latitude }} {{ longitude }}
          <button @click="editedStations.add(stationId)">
            Edit Station
          </button>
        </div>
        <div class="station-data" v-else>
          <input v-model="companies[companyId].stations[stationId].name" placeholder="Name">
          <input v-model="companies[companyId].stations[stationId].latitude" type="number" placeholder="Latitude">
          <input v-model="companies[companyId].stations[stationId].longitude" type="number" placeholder="Longitude">
          <button @click="updateStation(companyId, stationId)">
            Submit Station
          </button>
        </div>
        <button @click="deleteStation(companyId, stationId)">
          Delete Station
        </button>
      </div>

      <input v-model="newStations[companyId].name" placeholder="Name">
      <input v-model="newStations[companyId].latitude" type="number" placeholder="Latitude">
      <input v-model="newStations[companyId].longitude" type="number" placeholder="Longitude">
      <button @click="addStation(companyId)">
        Add Station
      </button>
    </div>
  </div>

  <div>
    <input v-model="newCompany.name" placeholder="Name">
    <input v-model="newCompany.parentCompanyId" type="number" placeholder="Parent Id">
    <button @click="addCompany">
      Add Company
    </button>
  </div>

  <div class="company-stations-view">
    <h1>
      Search company stations
    </h1>
    <input v-model="companyStationsView.companyId" type="number" placeholder="Company Id">
    <button @click="searchCompanyStations">
      Search Stations
    </button>
    <div v-for="{id : stationId, latitude, longitude, name, company} in companyStationsView.stations"
         class="station-entry"
    >
      {{ stationId }} {{ name }} {{ latitude }} {{ longitude }} Company: {{ company.name }}
    </div>
  </div>

  <div class="station-search-view">
    <h1>
      Search Stations in Radius
    </h1>
    <input v-model="stationsIdRadiusView.latitude" type="number" placeholder="Latitude">
    <input v-model="stationsIdRadiusView.longitude" type="number" placeholder="Longitude">
    <input v-model="stationsIdRadiusView.radiusKm" type="number" placeholder="Radius Km">
    <button @click="searchStationsInRadius">
      Search Stations
    </button>
    <div v-for="{id : stationId, latitude, longitude, name, company} in stationsIdRadiusView.stations"
         class="station-entry"
    >
      {{ stationId }} {{ name }} {{ latitude }} {{ longitude }} Company: {{ company.name }}
    </div>
  </div>
</template>

<style scoped>
h1 {
  margin: 1rem 0 1rem 0;
  font-weight: 500;
  font-size: 2.6rem;
  top: -10px;
}

h3 {
  font-size: 1.2rem;
}

input {
  margin: 0 0.2rem 0 0.2rem;
}

button {
  margin: 0 0.2rem 0 0.2rem;
}

input[type="number"] {
  width: 5rem;
}

.stations {
  padding-left: 2rem;
}

.station-entry, .company-entry {
  display: flex;
}

@media (min-width: 1024px) {
  .greetings h1,
  .greetings h3 {
    text-align: left;
  }
}
</style>
