import { TestBed } from "@angular/core/testing";
import {
  HttpClientTestingModule,
  HttpTestingController,
} from "@angular/common/http/testing";
import { CapacityApiService } from "./capacity-api.service";
import {
  CapacityGrid,
  TeamMember,
  CreateMemberRequest,
  UpdateMemberRequest,
  UpdateDaysOffRequest,
} from "../models/capacity.model";
import { environment } from "../../../../environments/environment";

describe("CapacityApiService", () => {
  let service: CapacityApiService;
  let httpMock: HttpTestingController;

  const BASE = `${environment.apiBaseUrl}/api/v1/capacity`;

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

  const mockMember: TeamMember = {
    id: "m3",
    name: "Charlie",
    role: "PDA",
    timeOverride: 1.0,
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [CapacityApiService],
    });
    service = TestBed.inject(CapacityApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it("should be created", () => {
    expect(service).toBeTruthy();
  });

  // -----------------------------------------------------------------------
  describe("getCapacityGrid()", () => {
    it("should GET /api/v1/capacity and return CapacityGrid", () => {
      service.getCapacityGrid().subscribe((grid) => {
        expect(grid).toEqual(mockGrid);
        expect(grid.members.length).toBe(2);
        expect(grid.sprints.length).toBe(2);
        expect(grid.daysPerSprint["Sprint 1"]).toBe(10);
      });

      const req = httpMock.expectOne(BASE);
      expect(req.request.method).toBe("GET");
      req.flush(mockGrid);
    });
  });

  // -----------------------------------------------------------------------
  describe("addMember()", () => {
    it("should POST /api/v1/capacity/members with correct body", () => {
      const body: CreateMemberRequest = { name: "Charlie", role: "PDA" };

      service.addMember(body).subscribe((member) => {
        expect(member).toEqual(mockMember);
      });

      const req = httpMock.expectOne(`${BASE}/members`);
      expect(req.request.method).toBe("POST");
      expect(req.request.body).toEqual(body);
      req.flush(mockMember);
    });
  });

  // -----------------------------------------------------------------------
  describe("updateMember()", () => {
    it("should PUT /api/v1/capacity/members/:id with correct body", () => {
      const body: UpdateMemberRequest = {
        name: "Charlie Updated",
        role: "DEV",
      };

      service.updateMember("m3", body).subscribe((member) => {
        expect(member.name).toBe("Charlie Updated");
      });

      const req = httpMock.expectOne(`${BASE}/members/m3`);
      expect(req.request.method).toBe("PUT");
      expect(req.request.body).toEqual(body);
      req.flush({ ...mockMember, name: "Charlie Updated", role: "DEV" });
    });

    it("should encode the member id in the URL", () => {
      const body: UpdateMemberRequest = { name: "Test", role: "QA" };

      service.updateMember("id with spaces", body).subscribe();

      const req = httpMock.expectOne(`${BASE}/members/id%20with%20spaces`);
      expect(req.request.method).toBe("PUT");
      req.flush(mockMember);
    });
  });

  // -----------------------------------------------------------------------
  describe("deleteMember()", () => {
    it("should DELETE /api/v1/capacity/members/:id", () => {
      service.deleteMember("m3").subscribe();

      const req = httpMock.expectOne(`${BASE}/members/m3`);
      expect(req.request.method).toBe("DELETE");
      req.flush(null);
    });
  });

  // -----------------------------------------------------------------------
  describe("updateDaysOff()", () => {
    it("should PUT /api/v1/capacity/days-off with correct body", () => {
      const body: UpdateDaysOffRequest = {
        teamMemberId: "m1",
        sprintName: "Sprint 1",
        daysOff: 3,
      };

      service.updateDaysOff(body).subscribe();

      const req = httpMock.expectOne(`${BASE}/days-off`);
      expect(req.request.method).toBe("PUT");
      expect(req.request.body).toEqual(body);
      req.flush(null);
    });
  });

  // -----------------------------------------------------------------------
  describe("updateDaysOffBulk()", () => {
    it("should PUT /api/v1/capacity/days-off/bulk with array body", () => {
      const requests: UpdateDaysOffRequest[] = [
        { teamMemberId: "m1", sprintName: "Sprint 1", daysOff: 3 },
        { teamMemberId: "m2", sprintName: "Sprint 2", daysOff: 1 },
      ];

      service.updateDaysOffBulk(requests).subscribe();

      const req = httpMock.expectOne(`${BASE}/days-off/bulk`);
      expect(req.request.method).toBe("PUT");
      expect(req.request.body).toEqual(requests);
      req.flush(null);
    });
  });
});
