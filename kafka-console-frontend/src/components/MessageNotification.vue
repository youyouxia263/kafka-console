<template>
  <transition name="notification">
    <div v-if="show" :class="['notification', type]">
      <i :class="iconClass"></i>
      <span class="message">{{ message }}</span>
    </div>
  </transition>
</template>

<script>
export default {
  name: 'MessageNotification',
  data() {
    return {
      show: false,
      message: '',
      type: 'info',
      timer: null
    };
  },
  computed: {
    iconClass() {
      const icons = {
        success: 'fas fa-check-circle',
        error: 'fas fa-times-circle',
        info: 'fas fa-info-circle',
        warning: 'fas fa-exclamation-circle'
      };
      return icons[this.type] || icons.info;
    }
  },
  methods: {
    showMessage(message, type = 'info', duration = 3000) {
      this.message = message;
      this.type = type;
      this.show = true;

      if (this.timer) {
        clearTimeout(this.timer);
      }

      this.timer = setTimeout(() => {
        this.show = false;
      }, duration);
    }
  },
  beforeUnmount() {
    if (this.timer) {
      clearTimeout(this.timer);
    }
  }
};
</script>

<style scoped>
.notification {
  position: fixed;
  top: 20px;
  right: 20px;
  padding: 15px 20px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  gap: 10px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  z-index: 9999;
}

.notification i {
  font-size: 1.2em;
}

.notification.success {
  background-color: #f0f9eb;
  color: #67c23a;
  border: 1px solid #c2e7b0;
}

.notification.error {
  background-color: #fef0f0;
  color: #f56c6c;
  border: 1px solid #fbc4c4;
}

.notification.info {
  background-color: #f4f4f5;
  color: #909399;
  border: 1px solid #e9e9eb;
}

.notification.warning {
  background-color: #fdf6ec;
  color: #e6a23c;
  border: 1px solid #f5dab1;
}

.notification-enter-active,
.notification-leave-active {
  transition: all 0.3s ease;
}

.notification-enter-from,
.notification-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
</style>