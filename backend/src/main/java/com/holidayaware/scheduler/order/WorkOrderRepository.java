package com.holidayaware.scheduler.order;

import org.springframework.data.jpa.repository.JpaRepository;

interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {
}
