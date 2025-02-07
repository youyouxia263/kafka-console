import { createRouter, createWebHashHistory } from "vue-router";
// import { routes } from "./routes";
import BrokerList from '@/components/BrokerList.vue';
import TopicList from '@/components/TopicList.vue';
import ConsumerGroupList from '@/components/ConsumerGroupList.vue';
import MessageList from '@/components/MessageList.vue';

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    {
      path: "/",
      redirect: "/brokers",
    },
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
  ],
});

export default router;
