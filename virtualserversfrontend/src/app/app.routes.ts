import { Routes } from '@angular/router';
import { AuthComponent } from './components/auth/auth.component';
import { ServersComponent } from './components/servers/servers.component';
import { AdminComponent } from './components/admin/admin.component';


export const routes: Routes = [
    {path: '', component: AuthComponent},
    {path: 'login', component: AuthComponent},
    {path: 'servers', component: ServersComponent},
    {path: 'admin', component: AdminComponent},
    {path: '**', redirectTo: 'login'}
];
