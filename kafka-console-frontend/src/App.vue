<template>
  <div id="app">
    <AppMenu />
    <main>
      <router-view></router-view>
    </main>
  </div>
</template>

<script>
import AppMenu from './components/Menu.vue';

export default {
  name: 'App',
  components: {
    AppMenu
  }
};
</script>

<style>
#app {
  display: flex;
}

nav {
  width: 200px;
}

main {
  flex: 1;
  padding: 2%;
  margin-left: 200px; /* 确保内容不在菜单栏下面 */
}
</style>