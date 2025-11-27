import { mount } from '@vue/test-utils';
import App from './App.vue';

describe('App.vue', () => {
  it('renders the app component', () => {
    const wrapper = mount(App);
    expect(wrapper.exists()).toBe(true);
  });

  it('has the correct root element id', () => {
    const wrapper = mount(App);
    expect(wrapper.find('#app').exists()).toBe(true);
  });

  it('contains router-view', () => {
    const wrapper = mount(App, {
      global: {
        stubs: ['router-view']
      }
    });
    expect(wrapper.findComponent({ name: 'RouterView' }).exists()).toBe(true);
  });
});
