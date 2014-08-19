/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package odin.domain;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Burndown {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	public Long getSprintId() {
		return sprintId;
	}

	public void setSprintId(Long sprintId) {
		this.sprintId = sprintId;
	}

	public Date getSnapshotDate() {
		return snapshotDate;
	}

	public void setSnapshotDate(Date snapshotDate) {
		this.snapshotDate = snapshotDate;
	}

	public Integer getSumEstimated() {
		return sumEstimated;
	}

	public void setSumEstimated(Integer sumEstimated) {
		this.sumEstimated = sumEstimated;
	}

	public Integer getSumLogged() {
		return sumLogged;
	}

	public void setSumLogged(Integer sumLogged) {
		this.sumLogged = sumLogged;
	}

	public Integer getSumRemaining() {
		return sumRemaining;
	}

	public void setSumRemaining(Integer sumRemaining) {
		this.sumRemaining = sumRemaining;
	}

	public Integer getSumIdealRemaining() {
		return sumIdealRemaining;
	}

	public void setSumIdealRemaining(Integer sumIdealRemaining) {
		this.sumIdealRemaining = sumIdealRemaining;
	}

	public Integer getSumTickets() {
		return sumTickets;
	}

	public void setSumTickets(Integer sumTickets) {
		this.sumTickets = sumTickets;
	}

	public Integer getSumStatusNew() {
		return sumStatusNew;
	}

	public void setSumStatusNew(Integer sumStatusNew) {
		this.sumStatusNew = sumStatusNew;
	}

	public Integer getSumStatusInProgress() {
		return sumStatusInProgress;
	}

	public void setSumStatusInProgress(Integer sumStatusInProgress) {
		this.sumStatusInProgress = sumStatusInProgress;
	}

	public Integer getSumStatusDone() {
		return sumStatusDone;
	}

	public void setSumStatusDone(Integer sumStatusDone) {
		this.sumStatusDone = sumStatusDone;
	}

	private Long sprintId;

	private Date snapshotDate;

	private Integer sumEstimated;

	private Integer sumLogged;

	private Integer sumRemaining;

	private Integer sumIdealRemaining;

	private Integer sumTickets;

	private Integer sumStatusNew;

	private Integer sumStatusInProgress;

	private Integer sumStatusDone;
}
