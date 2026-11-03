import { Routes } from '@angular/router';
import { LoginPageComponent } from './features/pages/login/login-page-component/login-page-component';
import { HomePageComponent } from './features/pages/home/home-page-component/home-page-component';
import { AppointmentPageComponent } from './features/pages/appointment/appointment-page-component/appointment-page-component';
import { authGuard } from './core/auth.guard';

export const routes: Routes = [
    {
        path: 'login',
        component: LoginPageComponent
    },
    {
        path: '',
        component: LoginPageComponent
    },
    {
        path: 'home',
        component: HomePageComponent,
        canActivate: [authGuard]
    },
    {
        path: 'appointments',
        component: AppointmentPageComponent,
        canActivate: [authGuard]
    }
];
