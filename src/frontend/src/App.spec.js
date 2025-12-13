import { mount } from '@vue/test-utils';
import App from './App.vue';

describe('App.vue', () => {
  const mountOptions = {
    global: {
      stubs: ['router-view']
    }
  };

  it('renders the app component', () => {
    const wrapper = mount(App, mountOptions);
    expect(wrapper.exists()).toBe(true);
  });

  it('has the correct root element id', () => {
    const wrapper = mount(App, mountOptions);
    expect(wrapper.find('#app').exists()).toBe(true);
  });

  it('contains router-view', () => {
    const wrapper = mount(App, mountOptions);
    expect(wrapper.html()).toContain('router-view');
  });
});
