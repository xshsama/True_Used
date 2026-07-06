import { createPinia } from 'pinia'
import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

// 自定义全局组件
import CategorySelect from './components/CategorySelect.vue'
import ImageUpload from './components/ImageUpload.vue'
import ProductCard from './components/ProductCard.vue'
import ProductCardSkeleton from './components/ProductCardSkeleton.vue'
import SellerCenter from './components/SellerCenter.vue'

// Vant UI
import {
  ActionSheet,
  AddressEdit,
  AddressList,
  Area,
  Badge,
  Button,
  Card,
  Cascader,
  Cell,
  CellGroup,
  Checkbox,
  CheckboxGroup,
  Collapse,
  CollapseItem,
  Dialog,
  Divider,
  Empty,
  Field,
  Form,
  Grid,
  GridItem,
  Icon,
  Lazyload,
  List,
  Loading,
  NavBar,
  NoticeBar,
  Picker,
  Popup,
  PullRefresh,
  Rate,
  Search,
  Skeleton,
  Swipe,
  SwipeCell,
  SwipeItem,
  Switch,
  Tab,
  Tabs,
  Tag,
  Toast,
  Uploader,
  Image as VanImage,
} from 'vant'
import 'vant/lib/index.css'

// 全局样式
import './styles/global.css'
import './styles/theme.css'
import './styles/variables.css'
// UnoCSS runtime (virtual CSS)
import 'virtual:uno.css'

const app = createApp(App)
const pinia = createPinia()

// 注册Vant组件
app.use(ActionSheet)
app.use(NavBar)
app.use(Search)
app.use(Swipe)
app.use(SwipeItem)
app.use(Grid)
app.use(GridItem)
app.use(Card)
app.use(Cascader)
app.use(Checkbox)
app.use(CheckboxGroup)
app.use(Button)
app.use(Icon)
app.use(Badge)
app.use(Cell)
app.use(CellGroup)
app.use(List)
app.use(PullRefresh)
app.use(VanImage)
app.use(Form)
app.use(Field)
app.use(Uploader)
app.use(Picker)
app.use(Popup)
app.use(Rate)
app.use(Dialog)
app.use(Toast)
app.use(Loading)
app.use(Empty)
app.use(Divider)
app.use(Tab)
app.use(Tabs)
app.use(NoticeBar)
app.use(Tag)
app.use(Switch)
app.use(SwipeCell)
app.use(Skeleton)
app.use(Collapse)
app.use(CollapseItem)
app.use(AddressEdit)
app.use(AddressList)
app.use(Area)
app.use(Lazyload)

// 注册自定义全局组件
app.component('CategorySelect', CategorySelect)
app.component('ImageUpload', ImageUpload)
app.component('ProductCard', ProductCard)
app.component('ProductCardSkeleton', ProductCardSkeleton)
app.component('SellerCenter', SellerCenter)

app.use(pinia)
app.use(router)

app.mount('#app')
