# Fix: cocoapods-sakddd-generate-preset-json calls sandbox.mt_on_demand_resources_pods
# Use: RUBYOPT="-r $(pwd)/patch_sandbox.rb" pod install
# or:  export RUBYOPT="-r /Users/delanding/ProjDoing/TDF/yijian/iosApp/patch_sandbox.rb"
at_exit { exit! } # prevent late cleanup issues with TracePoint

module PodSandboxFix
  def self.install!
    return unless defined?(Pod::Sandbox)
    return if Pod::Sandbox.method_defined?(:mt_on_demand_resources_pods)

    Pod::Sandbox.class_eval do
      attr_accessor :mt_on_demand_resources_pods
    end
    trace&.disable
  end

  def self.trace
    @trace ||= TracePoint.new(:class) do |tp|
      if tp.self.name == "Pod::Sandbox"
        install!
      end
    end
  end
end

PodSandboxFix.trace.enable
PodSandboxFix.install!