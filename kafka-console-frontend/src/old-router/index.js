import Vue from 'vue';
import Router from 'vue-router';
import BrokerList from '@/components/BrokerList.vue';
import TopicList from '@/components/TopicList.vue';
import ConsumerGroupList from '@/components/ConsumerGroupList.vue';
import MessageList from '@/components/MessageList.vue';

// Vue.use(Router);

export default new Router({
  routes: [
    {
      path: '/brokers',
      name: 'Brokers',
      component: BrokerList
    },
    {
      path: '/topics',
      name: 'Topics',
      component: TopicList
    },
    {
      path: '/consumer-groups',
      name: 'Consumer Groups',
      component: ConsumerGroupList
    },
    {
      path: '/messages',
      name: 'Messages',
      component: MessageList
    }
  ]
});