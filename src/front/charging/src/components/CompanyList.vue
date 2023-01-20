<script setup>
import axios from 'axios'
import {onMounted, ref} from 'vue'

defineProps({
  msg: {
    type: String,
    required: true
  }
})

const newCompany = ref({
  name: "",
  parentCompanyId: null
})
//companyId to template
const newStations = ref({})
const companies = ref({})

const editedStations = ref(new Set())
const editedCompanies = ref(new Set())


function addCompany() {
  axios.post('http://localhost:8080/company', newCompany.value)
      .then(response => {
        const company = response.data
        company.stations = {}

        companies.value[company.id] = company
        newStations.value[company.id] = {
          name: "",
          latitude: null,
          longitude: null,
          companyId: company.id
        }
      })
      .catch(error => {
        window.alert(error)
      })
}

function addStation(companyId) {
  axios.post('http://localhost:8080/station', newStations.value[companyId])
      .then(response => {
        const station = response.data

        companies.value[companyId]
            .stations[station.id] = station
      })
      .catch(error => {
        window.alert(error)
      })
}

function updateCompany(id) {
  axios.put(`http://localhost:8080/company`, companies.value[id])
      .then(response => {
        const company = response.data
        company.stations = companies.value[id].stations

        companies.value[id] = company

        editedCompanies.value.delete(id)
      })
      .catch(error => {
        window.alert(error)
      })
}

function updateStation(companyId, id) {
  axios.put(`http://localhost:8080/station`, companies.value[companyId].stations[id])
      .then(response => {
        const station = response.data

        companies.value[companyId].stations[id] = station

        editedStations.value.delete(id)
      })
      .catch(error => {
        window.alert(error)
      })
}

function deleteCompany(id) {
  axios.delete(`http://localhost:8080/company/${id}`)
      .then(_ => {
        delete companies.value[id]
        delete newStations.value[id]
      })
      .catch(error => {
        window.alert(error)
      })
}

function deleteStation(companyId, stationId) {
  axios.delete(`http://localhost:8080/station/${stationId}`)
      .then(_ => {
        const company = companies.value[companyId]
        company.stations = company.stations.filter(s => s.id !== stationId)
      })
      .catch(error => {
        window.alert(error)
      })
}
</script>

<template>
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
</template>

<style scoped>
h1 {
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
