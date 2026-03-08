package org.eclipse.cargotracker;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Status;
import jakarta.transaction.UserTransaction;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TxUtil {
    private static final Logger LOGGER = Logger.getLogger(TxUtil.class.getName());
    final UserTransaction ux;
    final EntityManager em;

    public TxUtil(UserTransaction ux, EntityManager em) {
        Objects.requireNonNull(ux, "UserTransaction must not be null");
        this.ux = ux;
        this.em = em;
    }

    public void begin() {
        try {
            this.ux.begin();
            if (this.em != null) {
                this.em.joinTransaction();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void commit() {
        try {
            if (this.ux.getStatus() == Status.STATUS_ACTIVE) {
                this.ux.commit();
            }
            LOGGER.log(Level.INFO, "commited tx status:{0}", new Object[]{this.ux.getStatus()});
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void rollback() {
        try {
            this.ux.rollback();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void runInTx(Runnable runnable) {
        try {
            begin();
            runnable.run();
            commit();
        } catch (Exception e) {
            rollback();
            throw new RuntimeException(e);
        }
    }

    public <T> T runInTx(Callable<T> callable) {
        try {
            begin();
            T result = callable.call();
            commit();
            return result;
        } catch (Exception e) {
            rollback();
            throw new RuntimeException(e);
        }
    }

}
