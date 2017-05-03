package com.novoda.merlin.rxjava;

import com.novoda.merlin.Merlin;
import com.novoda.merlin.NetworkStatus;
import com.novoda.merlin.registerable.bind.Bindable;
import com.novoda.merlin.registerable.connection.Connectable;
import com.novoda.merlin.registerable.disconnection.Disconnectable;

import rx.Emitter;
import rx.functions.Action1;
import rx.functions.Cancellable;

class MerlinAction implements Action1<Emitter<NetworkStatus.State>> {

    private Merlin merlin;

    MerlinAction(Merlin merlin) {
        this.merlin = merlin;
    }

    @Override
    public void call(Emitter<NetworkStatus.State> stateEmitter) {
        merlin.registerConnectable(createConnectable(stateEmitter));
        merlin.registerDisconnectable(createDisconnectable(stateEmitter));
        merlin.registerBindable(createBindable(stateEmitter));

        stateEmitter.setCancellation(createCancellable());

        merlin.bind();
    }

    private Connectable createConnectable(final Emitter<NetworkStatus.State> stateEmitter) {
        return new Connectable() {
            @Override
            public void onConnect() {
                stateEmitter.onNext(NetworkStatus.State.AVAILABLE);
            }
        };
    }

    private Disconnectable createDisconnectable(final Emitter<NetworkStatus.State> stateEmitter) {
        return new Disconnectable() {
            @Override
            public void onDisconnect() {
                stateEmitter.onNext(NetworkStatus.State.UNAVAILABLE);
            }
        };
    }

    private Bindable createBindable(final Emitter<NetworkStatus.State> stateEmitter) {
        return new Bindable() {
            @Override
            public void onBind(NetworkStatus networkStatus) {
                NetworkStatus.State current = networkStatus.isAvailable() ? NetworkStatus.State.AVAILABLE : NetworkStatus.State.UNAVAILABLE;
                stateEmitter.onNext(current);
            }
        };
    }

    private Cancellable createCancellable() {
        return new Cancellable() {
            @Override
            public void cancel() throws Exception {
                merlin.unbind();
            }
        };
    }
}
