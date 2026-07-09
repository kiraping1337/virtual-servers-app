import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ServerCardComponent } from './server-card.component';

describe('ServerCardComponent', () => {
  let component: ServerCardComponent;
  let fixture: ComponentFixture<ServerCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ServerCardComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ServerCardComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
