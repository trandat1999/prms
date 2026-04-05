import { Injectable } from '@angular/core';
import {Observable, Subject} from 'rxjs';
import { SignalType } from "../../shared/utils/signal.model";

@Injectable({
  providedIn: 'root',
})
export class SignalService {
  private signalSubject = new Subject<SignalType>();
  sendSignal(signal: SignalType) {
    this.signalSubject.next(signal);
  }
  subscribeToSignal(): Observable<any> {
    return this.signalSubject.asObservable();
  }
  constructor() { }
}
