const std = @import("std");

pub fn build(b: *std.Build) void {
    const target = b.standardTargetOptions(.{});
    const optimize = b.standardOptimizeOption(.{});

    const exe = b.addExecutable(.{
        .name = "nyx-vm",
        .target = target,
        .optimize = optimize
    });
    exe.addCSourceFiles(.{
        .files = &.{
            "src/chunk.c",
            "src/debug.c",
            "src/main.c"
        }
    });
    exe.linkLibC();

    b.installArtifact(exe);

    const run_exe = b.addRunArtifact(exe);

    const run_step = b.step("run", "Run the application");
    run_step.dependOn(&run_exe.step);
}
