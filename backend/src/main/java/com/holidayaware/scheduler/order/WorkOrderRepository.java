package com.holidayaware.scheduler.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

interface WorkOrderRepository extends JpaRepository<WorkOrder, Long>, JpaSpecificationExecutor<WorkOrder> {
}
