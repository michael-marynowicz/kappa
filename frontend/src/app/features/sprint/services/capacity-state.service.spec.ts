import { TestBed } from "@angular/core/testing";
import { of, throwError } from "rxjs";
import { CapacityStateService } from "./capacity-state.service";
import { CapacityApiService } from "./capacity-api.service";
import { CapacityGrid, TeamMember } from "../models/capacity.model";

describe("CapacityStateService", () => {
  let service: CapacityStateService;
  let apiMock: jasmine.SpyObj<CapacityApiService>;

  const mockGrid: CapacityGrid = {
    members: [
      { id: "m1", name: "Alice", role: "DEV", timeOverride: 1.0 },
      { id: "m2", name: "Bob", role: "QA", timeOverride: 0.5 },
    ],
    sprints: ["Sprint 1", "Sprint 2"],
    daysPerSprint: { "Sprint 1": 10, "Sprint 2": 9 },
    daysOffGrid: {
      m1: { "Sprint 1": 2, "Sprint 2": 0 },
      m2: { "Sprint 1": 1, "Sprint 2": 3 },
    },
    sprintDetails: {
      "Sprint 1": { pi: "1.0", iteration: 1, ip: false },
      "Sprint 2": { pi: "1.0", iteration: 2, ip: false },
    },
  };

  beforeEach(() => {
    apiMock = jasmine.createSpyObj("CapacityApiService", [
      "getCapacityGrid",
      "addMember",
      "updateMember",
      "deleteMember",
      "updateDaysOff",
      "updateDaysOffBulk",
    ]);

    TestBed.configureTestingModule({
      providers: [
        CapacityStateService,
        { provide: CapacityApiService, useValue: apiMock },
      ],
    });

    service = TestBed.inject(CapacityStateService);
  });

  it("should be created with initial null/false state", () => {
    expect(service).toBeTruthy();
    expect(service.grid()).toBeNull();
    expect(service.loading()).toBeFalse();
    expect(service.error()).toBeNull();
    expect(service.savingCell()).toBeNull();
  });

  // -----------------------------------------------------------------------
  describe("loadGrid()", () => {
    it("should populate grid signal on success", () => {
      apiMock.getCapacityGrid.and.returnValue(of(mockGrid));

      service.loadGrid();

      expect(service.grid()).toEqual(mockGrid);
      expect(service.loading()).toBeFalse();
      expect(service.error()).toBeNull();
    });

    it("should set error on failure", () => {
      apiMock.getCapacityGrid.and.returnValue(
        throwError(() => new Error("Load failed")),
      );

      service.loadGrid();

      expect(service.grid()).toBeNull();
      expect(service.error()).toBe("Load failed");
      expect(service.loading()).toBeFalse();
    });
  });

  // -----------------------------------------------------------------------
  describe("addMember()", () => {
    it("should call API and reload grid on success", () => {
      apiMock.addMember.and.returnValue(
        of({ id: "m3", name: "Charlie", role: "PDA", timeOverride: 1.0 }),
      );
      apiMock.getCapacityGrid.and.returnValue(of(mockGrid));

      service.addMember("Charlie", "PDA");

      expect(apiMock.addMember).toHaveBeenCalledWith({
        name: "Charlie",
        role: "PDA",
      });
      expect(apiMock.getCapacityGrid).toHaveBeenCalled();
    });

    it("should set error on failure", () => {
      apiMock.addMember.and.returnValue(
        throwError(() => new Error("Add failed")),
      );

      service.addMember("Charlie", "PDA");

      expect(service.error()).toBe("Add failed");
    });
  });

  // -----------------------------------------------------------------------
  describe("updateMember()", () => {
    it("should call API and reload grid on success", () => {
      const member: TeamMember = {
        id: "m1",
        name: "Alice",
        role: "DEV",
        timeOverride: 1.0,
      };
      apiMock.updateMember.and.returnValue(
        of({ id: "m1", name: "Alice Updated", role: "PDA", timeOverride: 1.0 }),
      );
      apiMock.getCapacityGrid.and.returnValue(of(mockGrid));

      service.updateMember(member, "Alice Updated", "PDA");

      expect(apiMock.updateMember).toHaveBeenCalledWith("m1", {
        name: "Alice Updated",
        role: "PDA",
      });
      expect(apiMock.getCapacityGrid).toHaveBeenCalled();
    });

    it("should set error on failure", () => {
      const member: TeamMember = {
        id: "m1",
        name: "Alice",
        role: "DEV",
        timeOverride: 1.0,
      };
      apiMock.updateMember.and.returnValue(
        throwError(() => new Error("Update failed")),
      );

      service.updateMember(member, "Alice Updated", "PDA");

      expect(service.error()).toBe("Update failed");
    });
  });

  // -----------------------------------------------------------------------
  describe("deleteMember()", () => {
    it("should call API and reload grid on success", () => {
      apiMock.deleteMember.and.returnValue(of(void 0));
      apiMock.getCapacityGrid.and.returnValue(of(mockGrid));

      service.deleteMember("m1");

      expect(apiMock.deleteMember).toHaveBeenCalledWith("m1");
      expect(apiMock.getCapacityGrid).toHaveBeenCalled();
    });

    it("should set error on failure", () => {
      apiMock.deleteMember.and.returnValue(
        throwError(() => new Error("Delete failed")),
      );

      service.deleteMember("m1");

      expect(service.error()).toBe("Delete failed");
    });
  });

  // -----------------------------------------------------------------------
  describe("updateDaysOff()", () => {
    it("should call API and optimistically update grid on success", () => {
      apiMock.getCapacityGrid.and.returnValue(of(mockGrid));
      service.loadGrid();

      apiMock.updateDaysOff.and.returnValue(of(void 0));

      service.updateDaysOff("m1", "Sprint 1", 5);

      expect(apiMock.updateDaysOff).toHaveBeenCalledWith({
        teamMemberId: "m1",
        sprintName: "Sprint 1",
        daysOff: 5,
      });
      // Optimistic update
      expect(service.grid()!.daysOffGrid["m1"]["Sprint 1"]).toBe(5);
      expect(service.savingCell()).toBeNull();
    });

    it("should set error and clear savingCell on failure", () => {
      apiMock.getCapacityGrid.and.returnValue(of(mockGrid));
      service.loadGrid();

      apiMock.updateDaysOff.and.returnValue(
        throwError(() => new Error("Save failed")),
      );

      service.updateDaysOff("m1", "Sprint 1", 5);

      expect(service.error()).toBe("Save failed");
      expect(service.savingCell()).toBeNull();
    });
  });

  // -----------------------------------------------------------------------
  describe("clearError()", () => {
    it("should reset error to null", () => {
      apiMock.getCapacityGrid.and.returnValue(
        throwError(() => new Error("Err")),
      );
      service.loadGrid();
      expect(service.error()).toBeTruthy();

      service.clearError();

      expect(service.error()).toBeNull();
    });
  });
});
