<script setup>
import TaskList from './components/CompanyList.vue'
</script>

<template>
    <div class="wrapper">
      <TaskList/>
    </div>
</template>

<style scoped>
header {
  line-height: 1.5;
}

@media (min-width: 1024px) {
  header {
    display: flex;
    place-items: center;
    padding-right: calc(var(--section-gap) / 2);
  }
}
</style>
