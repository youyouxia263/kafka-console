import Vue from 'vue'
import Router from 'vue-router'
import ClusterConfig from '@/components/ClusterConfig.vue'
import TopicManagement from '@/components/TopicManagement.vue'
import ConsumerGroupManagement from '@/components/ConsumerGroupManagement.vue'
import MessageManagement from '@/components/MessageManagement.vue'

Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/cluster-config',
      name: 'ClusterConfig',
      component: ClusterConfig
    },
    {
      path: '/topic-management',
      name: 'TopicManagement',
      component: TopicManagement
    },
    {
      path: '/consumer-group-management',
      name: 'ConsumerGroupManagement',
      component: ConsumerGroupManagement
    },
    {
      path: '/message-management',
      name: 'MessageManagement',
      component: MessageManagement
    }
  ]
})