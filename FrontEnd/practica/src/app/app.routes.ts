import { Routes } from '@angular/router';
import { LoginPageComponent } from './pages/login-page-component/login-page-component';

export const routes: Routes = [
    {
        path: 'login',
        component: LoginPageComponent
    },
    {
        path: '',
        component: LoginPageComponent
    }
];
