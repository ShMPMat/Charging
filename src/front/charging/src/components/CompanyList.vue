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
const companies = ref([])


function addCompany() {
  axios.post('http://localhost:8080/company', newCompany.value)
      .then(response => {
        const company = response.data
        company.stations = []

        companies.value.push(company)
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

        companies.value.find(c => c.id === companyId)
            .stations.push(station)
      })
      .catch(error => {
        window.alert(error)
      })
}

function deleteCompany(id) {
  axios.delete(`http://localhost:8080/company/${id}`)
      .then(_ => {
        companies.value = companies.value.filter(c => c.id !== id)
        delete newStations.value[id]
      })
      .catch(error => {
        window.alert(error)
      })
}

function deleteStation(companyId, stationId) {
  axios.delete(`http://localhost:8080/station/${stationId}`)
      .then(_ => {
        const company = companies.value.find(c => c.id === companyId)
        company.stations = company.stations.filter(s => s.id !== stationId)
      })
      .catch(error => {
        window.alert(error)
      })
}
</script>

<template>
  <div v-for="{id : companyId, name, parentCompany, stations} in companies">
    {{ companyId }} {{ name }} {{ parentCompany?.id }}
    <button @click="deleteCompany(companyId)">
      Delete Company
    </button>

    <div class="stations">
    <div v-for="{id : stationId, latitude, longitude, name} in stations">
      {{ stationId }} {{ name }} {{ latitude }} {{ longitude }}
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

input[type="number"] {
  width: 5rem;
}

.stations {
  padding-left: 2rem;
}

@media (min-width: 1024px) {
  .greetings h1,
  .greetings h3 {
    text-align: left;
  }
}
</style>
