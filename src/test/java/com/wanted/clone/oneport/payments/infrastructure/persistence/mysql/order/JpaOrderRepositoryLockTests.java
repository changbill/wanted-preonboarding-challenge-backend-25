package com.wanted.clone.oneport.payments.infrastructure.persistence.mysql.order;

import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Lock;

class JpaOrderRepositoryLockTests {

    @Test
    void findByIdForUpdate_UsesPessimisticWriteLock() throws NoSuchMethodException {
        Lock lock = JpaOrderRepository.class
            .getMethod("findByIdForUpdate", String.class)
            .getAnnotation(Lock.class);

        Assertions.assertNotNull(lock);
        Assertions.assertEquals(LockModeType.PESSIMISTIC_WRITE, lock.value());
    }
}
